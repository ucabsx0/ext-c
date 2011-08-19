/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
Copyright (c) 2010, Keith Cassell
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following 
      disclaimer in the documentation and/or other materials
      provided with the distribution.
    * Neither the name of the Victoria University of Wellington
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package nz.ac.vuw.ecs.kcassell.persistence;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.metrics.persistence.Database;
import net.sourceforge.metrics.persistence.IDatabaseConstants;

public class RecordInserter implements IDatabaseConstants  {

	/**
	 * Save metric values for the indicated Java element and all of its
	 * subelements to a database.
	 * @param element
	 *            the highest level element to be saved, e.g. a project
	 * @throws InvocationTargetException
	 * @throws SQLException 
	 */
	public void saveMeasurementsToDB(Collection<SoftwareMeasurement> measurements)
	// , IProgressMonitor monitor)
			throws InvocationTargetException, SQLException {
		Database db = new Database();
		db.loadDriver();
		Connection connection = null;
		/* Store the Statement and Prepared statement object references
		 * in a list for convenience. */
		List<Statement> statements = new ArrayList<Statement>();
		Statement statement = null;
		ResultSet resultSet = null;
		
		/* Statements, PreparedStatements, Connections and ResultSets
		 * are resources that should be released explicitly after use, hence the
		 * try-catch-finally pattern used below. */
		try {
			connection = db.prepareConnection();

			/* Creating a statement object that we can use for running various
			 * SQL statements commands against the database. */
			statement = connection.createStatement();
			statements.add(statement);
			PreparedStatement deleteStatement =
				addDeleteMetricValuesPreparedStatement(connection, statements);
			PreparedStatement insertStatement =
				addInsertMetricValuesPreparedStatement(connection, statements);
			
			for (SoftwareMeasurement measurement : measurements) {
				String handle = measurement.getHandle();
				Double value = measurement.getMeasurement();
				String metricId = measurement.getMetricId();
				Integer prefKey = measurement.getPrefKey();
				saveMetricValue(deleteStatement, insertStatement, metricId, value,
						handle, prefKey);
			}

			statement.close();
			connection.commit();
			System.out.println("Committed the transaction");

			// In embedded mode, an application should shut down the database.
			db.shutDownEmbedded();
		} catch (SQLException sqle) {
			Database.printSQLException(sqle);
			throw sqle;
		} finally {
			// release all open resources to avoid unnecessary memory usage
			db.releaseResources(connection, statements, resultSet);
		}
	}

	private PreparedStatement addDeleteMetricValuesPreparedStatement(
			Connection connection, List<Statement> statements)
			throws SQLException {
		String sqlString = DELETE + METRIC_VALUES_TABLE +
		WHERE + HANDLE_FIELD + " = ? " + AND  + ACRONYM_FIELD + " = ? " +
		AND + USER_PREFERENCES_FOREIGN_KEY + " = ?";
		PreparedStatement statement =
			connection.prepareStatement(sqlString);
		statements.add(statement);
		return statement;
	}

	private PreparedStatement addInsertMetricValuesPreparedStatement(
			Connection connection, List<Statement> statements)
			throws SQLException {
		// Values: handle, metricId, value, preferenceKey
		String sqlString =
			INSERT + METRIC_VALUES_TABLE + VALUES + "(?, ?, ?, ?)";
		PreparedStatement statement = connection.prepareStatement(sqlString);
		statements.add(statement);
		return statement;
	}

	private void saveMetricValue(PreparedStatement deleteStatement,
			PreparedStatement insertStatement, String metricId, double value,
			String handle, int prefKey) {
		deleteOldMetricValue(deleteStatement, metricId, handle, prefKey);
		insertNewMetricValue(insertStatement, metricId, value, handle, prefKey);
	}

	private void deleteOldMetricValue(PreparedStatement deleteStatement,
			String metricId, String handle, int prefKey) {
		// Delete old metric values from prior runs
		try {
			deleteStatement.setString(1, handle);
			deleteStatement.setString(2, metricId);
			deleteStatement.setInt(3, prefKey);
			deleteStatement.executeUpdate();
		} catch (SQLException e) {
			// quietly swallow the exception. In many cases, there will be
			// nothing in the database to delete
		}
	}
	
	private void insertNewMetricValue(PreparedStatement insertStatement,
			String metricId, double value, String handle, int prefKey) {
		// Insert the new values
		try {
			insertStatement.setString(1, handle);
			insertStatement.setString(2, metricId);
			insertStatement.setDouble(3, value);
			insertStatement.setInt(4, prefKey);
			insertStatement.executeUpdate();
		} catch (SQLException e) {
			Database.printSQLException(e);
		}
	}


}
