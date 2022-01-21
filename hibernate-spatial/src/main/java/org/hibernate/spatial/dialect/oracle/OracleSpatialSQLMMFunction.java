/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

package org.hibernate.spatial.dialect.oracle;

import java.util.List;

import org.hibernate.query.sqm.produce.function.FunctionReturnTypeResolver;
import org.hibernate.query.sqm.produce.function.StandardArgumentsValidators;
import org.hibernate.sql.ast.SqlAstNodeRenderingMode;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.type.SqlTypes;

public class OracleSpatialSQLMMFunction extends OracleSpatialFunction {

	private final String stMethod;
	private final boolean addGeomAccessor;

	public OracleSpatialSQLMMFunction(
			String name,
			String stMethod,
			int numArgs,
			FunctionReturnTypeResolver returnTypeResolver,
			boolean addGeomAccessor) {
		super(
				name,
				true,
				StandardArgumentsValidators.exactly( numArgs ),
				returnTypeResolver
		);
		this.stMethod = stMethod;
		this.addGeomAccessor = addGeomAccessor;
	}

	public OracleSpatialSQLMMFunction(
			String name,
			String stMethod,
			int numArgs,
			FunctionReturnTypeResolver returnTypeResolver) {
		this(
				name,
				stMethod,
				numArgs,
				returnTypeResolver,
				false
		);
	}

	@Override
	public void render(
			SqlAppender sqlAppender,
			List<? extends SqlAstNode> arguments,
			SqlAstTranslator<?> walker) {
		final Expression geometry = (Expression) arguments.get( 0 );

		sqlAppender.appendSql( "ST_GEOMETRY(" );
		walker.render( geometry, SqlAstNodeRenderingMode.DEFAULT);
		sqlAppender.appendSql( ")." );
		sqlAppender.appendSql( stMethod );
		sqlAppender.appendSql( "(" );
		for ( int i = 1; i < arguments.size(); i++ ) {
			Expression param = (Expression) arguments.get( i );

			if ( param.getExpressionType().getJdbcMappings().get( 0 ).getJdbcType()
					.getDefaultSqlTypeCode() == SqlTypes.GEOMETRY ) {
				sqlAppender.appendSql( "ST_GEOMETRY(" );
				walker.render( param, SqlAstNodeRenderingMode.DEFAULT);
				sqlAppender.appendSql( ")" );
			}
			else {
				walker.render( param, SqlAstNodeRenderingMode.DEFAULT);
			}
		}
		sqlAppender.appendSql( ")" );
		if ( addGeomAccessor ) {
			sqlAppender.appendSql( ".geom " );
		}
	}


}
