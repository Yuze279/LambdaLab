public interface Expression {

	
	
	Expression copy();

    Expression sub(Variable v, Expression e);

    boolean equals(Expression other);
}