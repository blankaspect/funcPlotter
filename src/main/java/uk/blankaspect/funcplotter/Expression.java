/*====================================================================*\

Expression.java

Expression class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uk.blankaspect.common.exception.AppException;

//----------------------------------------------------------------------


// EXPRESSION CLASS


class Expression
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	VARIABLE_STR	= "x";

	// Lexical analyser states
	private enum LexState
	{
		START,
		WHITESPACE,
		NUMBER,
		ALPHA,
		SYMBOL,
		EOL,
		DONE,
		INVALID
	}

	// Numeric token states
	private enum NumericState
	{
		SIGNIFICAND,
		EXP_INDICATOR,
		EXPONENT
	}

	// Parser states
	private enum ParseState
	{
		OPERAND,
		OPERATION,
		DONE
	}

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// UNARY OPERATION


	private enum UnaryOperation
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		ABS
		(
			"abs"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.abs(operand);
			}
		},

		ACOS
		(
			"acos"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.acos(operand);
			}
		},

		ACOSH
		(
			"acosh"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return ((operand < 1.0) ? Double.NaN
										: Math.log(operand + Math.sqrt(operand * operand - 1.0)));
			}
		},

		ACOT
		(
			"acot"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.atan(1.0 / operand);
			}
		},

		ACSC
		(
			"acsc"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.asin(1.0 / operand);
			}
		},

		ASEC
		(
			"asec"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.acos(1.0 / operand);
			}
		},

		ASIN
		(
			"asin"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.asin(operand);
			}
		},

		ASINH
		(
			"asinh"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.log(operand + Math.sqrt(operand * operand + 1.0));
			}
		},

		ATAN
		(
			"atan"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.atan(operand);
			}
		},

		ATANH
		(
			"atanh"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return (((operand >= -1.0) && (operand <= 1.0))
													? 0.5 * Math.log((1.0 + operand) / (1.0 - operand))
													: Double.NaN);
			}
		},

		CEIL
		(
			"ceil"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.ceil(operand);
			}
		},

		COS
		(
			"cos"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.cos(operand);
			}
		},

		COSH
		(
			"cosh"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return (0.5 * (Math.exp(operand) + Math.exp(-operand)));
			}
		},

		COT
		(
			"cot"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return (1.0 / Math.tan(operand));
			}
		},

		CSC
		(
			"csc"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return (1.0 / Math.sin(operand));
			}
		},

		EXP
		(
			"exp"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.exp(operand);
			}
		},

		FLOOR
		(
			"floor"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.floor(operand);
			}
		},

		LG
		(
			"lg"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return (Math.log10(operand));
			}
		},

		LN
		(
			"ln"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.log(operand);
			}
		},

		ROUND
		(
			"round"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.rint(operand);
			}
		},

		SEC
		(
			"sec"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return (1.0 / Math.cos(operand));
			}
		},

		SIN
		(
			"sin"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.sin(operand);
			}
		},

		SINH
		(
			"sinh"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return (0.5 * (Math.exp(operand) - Math.exp(-operand)));
			}
		},

		SQRT
		(
			"sqrt"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.sqrt(operand);
			}
		},

		TAN
		(
			"tan"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return Math.tan(operand);
			}
		},

		TANH
		(
			"tanh"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				double exp2X = Math.exp(2.0 * operand);
				return ((exp2X - 1) / (exp2X + 1));
			}
		},

		PLUS
		(
			"plus"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return operand;
			}
		},

		MINUS
		(
			"minus"
		)
		{
			@Override
			protected double evaluate(double operand)
			{
				return -operand;
			}
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private UnaryOperation(String key)
		{
			this.key = key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract double evaluate(double operand);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;

	}

	//==================================================================


	// BINARY OPERATION


	private enum BinaryOperation
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		ADD
		(
			"add",
			0
		)
		{
			@Override
			protected double evaluate(double operand1,
									  double operand2)
			{
				return (operand1 + operand2);
			}
		},

		SUBTRACT
		(
			"subtract",
			0
		)
		{
			@Override
			protected double evaluate(double operand1,
									  double operand2)
			{
				return (operand1 - operand2);
			}
		},

		MULTIPLY
		(
			"multiply",
			1
		)
		{
			@Override
			protected double evaluate(double operand1,
									  double operand2)
			{
				return (operand1 * operand2);
			}
		},

		DIVIDE
		(
			"divide",
			1
		)
		{
			@Override
			protected double evaluate(double operand1,
									  double operand2)
			{
				return (operand1 / operand2);
			}
		},

		REMAINDER
		(
			"remainder",
			1
		)
		{
			@Override
			protected double evaluate(double operand1,
									  double operand2)
			{
				return (operand1 % operand2);
			}
		},

		IEEE_REMAINDER
		(
			"ieeeRemainder",
			1
		)
		{
			@Override
			protected double evaluate(double operand1,
									  double operand2)
			{
				return (Math.IEEEremainder(operand1, operand2));
			}
		},

		POWER
		(
			"power",
			2
		)
		{
			@Override
			protected double evaluate(double operand1,
									  double operand2)
			{
				return Math.pow(operand1, operand2);
			}
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private BinaryOperation(String key,
								int    precedence)
		{
			this.key = key;
			this.precedence = precedence;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract double evaluate(double operand1,
										   double operand2);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	int		precedence;

	}

	//==================================================================


	// KEYWORD


	private enum Keyword
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		ABS
		(
			"abs",
			UnaryOperation.ABS
		),

		ACOS
		(
			"acos",
			UnaryOperation.ACOS
		),

		ACOSH
		(
			"acosh",
			UnaryOperation.ACOSH
		),

		ACOT
		(
			"acot",
			UnaryOperation.ACOT
		),

		ACSC
		(
			"acsc",
			UnaryOperation.ACSC
		),

		ASEC
		(
			"asec",
			UnaryOperation.ASEC
		),

		ASIN
		(
			"asin",
			UnaryOperation.ASIN
		),

		ASINH
		(
			"asinh",
			UnaryOperation.ASINH
		),

		ATAN
		(
			"atan",
			UnaryOperation.ATAN
		),

		ATANH
		(
			"atanh",
			UnaryOperation.ATANH
		),

		CEIL
		(
			"ceil",
			UnaryOperation.CEIL
		),

		COS
		(
			"cos",
			UnaryOperation.COS
		),

		COSH
		(
			"cosh",
			UnaryOperation.COSH
		),

		COT
		(
			"cot",
			UnaryOperation.COT
		),

		CSC
		(
			"csc",
			UnaryOperation.CSC
		),

		EXP
		(
			"exp",
			UnaryOperation.EXP
		),

		FLOOR
		(
			"floor",
			UnaryOperation.FLOOR
		),

		LG
		(
			"lg",
			UnaryOperation.LG
		),

		LN
		(
			"ln",
			UnaryOperation.LN
		),

		ROUND
		(
			"round",
			UnaryOperation.ROUND
		),

		SEC
		(
			"sec",
			UnaryOperation.SEC
		),

		SIN
		(
			"sin",
			UnaryOperation.SIN
		),

		SINH
		(
			"sinh",
			UnaryOperation.SINH
		),

		SQRT
		(
			"sqrt",
			UnaryOperation.SQRT
		),

		TAN
		(
			"tan",
			UnaryOperation.TAN
		),

		TANH
		(
			"tanh",
			UnaryOperation.TANH
		),

		E
		(
			"e"
		),

		PI
		(
			"pi"
		);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Keyword(String key)
		{
			this(key, null);
		}

		//--------------------------------------------------------------

		private Keyword(String         key,
						UnaryOperation operation)
		{
			this.key = key;
			this.operation = operation;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static Keyword forKey(String key)
		{
			for (Keyword value : values())
			{
				if (value.key.equals(key))
					return value;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String			key;
		private	UnaryOperation	operation;

	}

	//==================================================================


	// SYMBOL


	private enum Symbol
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		PLUS
		(
			'+',
			UnaryOperation.PLUS,
			BinaryOperation.ADD
		),

		MINUS
		(
			'-',
			UnaryOperation.MINUS,
			BinaryOperation.SUBTRACT
		),

		ASTERISK
		(
			'*',
			BinaryOperation.MULTIPLY
		),

		SLASH
		(
			'/',
			BinaryOperation.DIVIDE
		),

		PERCENT
		(
			'%',
			BinaryOperation.REMAINDER
		),

		BACKSLASH
		(
			'\\',
			BinaryOperation.IEEE_REMAINDER
		),

		CARET
		(
			'^',
			BinaryOperation.POWER
		),

		OPENING_PARENTHESIS
		(
			'('
		),

		CLOSING_PARENTHESIS
		(
			')'
		);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Symbol(char key)
		{
			this(key, null, null);
		}

		//--------------------------------------------------------------

		private Symbol(char            key,
					   BinaryOperation binaryOperation)
		{
			this(key, null, binaryOperation);
		}

		//--------------------------------------------------------------

		private Symbol(char            key,
					   UnaryOperation  unaryOperation,
					   BinaryOperation binaryOperation)
		{
			this.key = key;
			this.unaryOperation = unaryOperation;
			this.binaryOperation = binaryOperation;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static Symbol forKey(char key)
		{
			for (Symbol symbol : values())
			{
				if (symbol.key == key)
					return symbol;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	char			key;
		private	UnaryOperation	unaryOperation;
		private	BinaryOperation	binaryOperation;

	}

	//==================================================================


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		CHARACTER_NOT_ALLOWED
		("The character '%1' is not allowed in an expression."),

		INVALID_NUMBER
		("\"%1\" is not a valid number."),

		UNRECOGNISED_TOKEN
		("The token \"%1\" is not recognised."),

		OPERAND_EXPECTED
		("An operand was expected."),

		BINARY_OPERATION_EXPECTED
		("A binary operation was expected."),

		UNEXPECTED_CLOSING_PARENTHESIS
		("An unexpected ')' was found."),

		CLOSING_PARENTHESIS_EXPECTED
		("A ')' was expected.");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// EXPRESSION EXCEPTION CLASS


	public static class Exception
		extends AppException
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	INDICATOR_STR	= "^";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Exception(ErrorId id,
						  int     offset)
		{
			super(id);
			this.offset = offset;
		}

		//--------------------------------------------------------------

		private Exception(ErrorId id,
						  String  str,
						  int     offset)
		{
			super(id);
			setReplacements(str);
			this.offset = offset;
		}

		//--------------------------------------------------------------

		private Exception(String str,
						  int    offset)
		{
			super(str);
			this.offset = offset;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public int getOffset()
		{
			return offset;
		}

		//--------------------------------------------------------------

		public String getIndicatorString()
		{
			return (offset == 0) ? INDICATOR_STR : " ".repeat(offset) + INDICATOR_STR;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int	offset;

	}

	//==================================================================


	// TOKEN CLASS


	private static class Token
	{

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// NUMBER TOKEN


		private static class NumberToken
			extends Token
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private NumberToken(int    offset,
								double value)
			{
				super(offset);
				this.value = value;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public String toString()
			{
				return Double.toString(value);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	double	value;

		}

		//==============================================================


		// VARIABLE TOKEN


		private static class VariableToken
			extends Token
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private VariableToken(int offset)
			{
				super(offset);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public String toString()
			{
				return VARIABLE_STR;
			}

			//----------------------------------------------------------

		}

		//==============================================================


		// KEYWORD TOKEN


		private static class KeywordToken
			extends Token
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	String	INVALID_KEYWORD_STR	= "<invalid keyword>";

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private KeywordToken(int     offset,
								 Keyword keyword)
			{
				super(offset);
				this.keyword = keyword;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public String toString()
			{
				return (keyword == null) ? INVALID_KEYWORD_STR : keyword.key;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	Keyword	keyword;

		}

		//==============================================================


		// SYMBOL TOKEN


		private static class SymbolToken
			extends Token
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	String	INVALID_SYMBOL_STR	= "<invalid symbol>";

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private SymbolToken(int    offset,
								Symbol symbol)
			{
				super(offset);
				this.symbol = symbol;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public String toString()
			{
				return (symbol == null) ? INVALID_SYMBOL_STR : Character.toString(symbol.key);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	Symbol	symbol;

		}

		//==============================================================


		// END-OF-FILE TOKEN


		private static class EofToken
			extends Token
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	String	END_STR	= "<end>";

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private EofToken(int offset)
			{
				super(offset);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public String toString()
			{
				return END_STR;
			}

			//----------------------------------------------------------

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Token(int offset)
		{
			this.offset = offset;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int	offset;

	}

	//==================================================================


	// NODE CLASS


	private static class Node
	{

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// CONSTANT NODE


		private static class ConstantNode
			extends Node
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	String	KIND_STR	= "constant";

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private ConstantNode(Node   parent,
								 double value)
			{
				super(parent);
				this.value = value;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;

				return (obj instanceof ConstantNode other) && (value == other.value) && super.equals(other);
			}

			//----------------------------------------------------------

			@Override
			public int hashCode()
			{
				long bits = Double.doubleToLongBits(value);
				return 31 * super.hashCode() + ((int)bits ^ (int)(bits >> 32));
			}

			//----------------------------------------------------------

			@Override
			protected boolean isTerminal()
			{
				return true;
			}

			//----------------------------------------------------------

			@Override
			protected String getKindString()
			{
				return KIND_STR;
			}

			//----------------------------------------------------------

			@Override
			protected String getValueString()
			{
				return Double.toString(value);
			}

			//----------------------------------------------------------

			@Override
			protected double evaluate(double x)
			{
				return value;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	double	value;

		}

		//==============================================================


		// VARIABLE NODE


		private static class VariableNode
			extends Node
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	String	KIND_STR	= "variable";

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private VariableNode(Node parent)
			{
				super(parent);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;

				return (obj instanceof VariableNode other) && super.equals(other);
			}

			//----------------------------------------------------------

			@Override
			protected boolean isTerminal()
			{
				return true;
			}

			//----------------------------------------------------------

			@Override
			protected String getKindString()
			{
				return KIND_STR;
			}

			//----------------------------------------------------------

			@Override
			protected String getValueString()
			{
				return VARIABLE_STR;
			}

			//----------------------------------------------------------

			@Override
			protected double evaluate(double x)
			{
				return x;
			}

			//----------------------------------------------------------

		}

		//==============================================================


		// UNARY OPERATION NODE


		private static class UnaryOperationNode
			extends Node
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	String	KIND_STR	= "unaryOperation";

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private UnaryOperationNode(Node           parent,
									   UnaryOperation unaryOperation)
			{
				super(parent);
				this.unaryOperation = unaryOperation;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;

				return (obj instanceof UnaryOperationNode other) && (unaryOperation == other.unaryOperation)
						&& super.equals(other);
			}

			//----------------------------------------------------------

			@Override
			public int hashCode()
			{
				return 31 * super.hashCode() + unaryOperation.ordinal();
			}

			//----------------------------------------------------------

			@Override
			protected String getKindString()
			{
				return KIND_STR;
			}

			//----------------------------------------------------------

			@Override
			protected String getValueString()
			{
				return unaryOperation.toString();
			}

			//----------------------------------------------------------

			@Override
			protected double evaluate(double x)
			{
				return unaryOperation.evaluate(leftChild.evaluate(x));
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	UnaryOperation	unaryOperation;

		}

		//==============================================================


		// BINARY OPERATION NODE


		private static class BinaryOperationNode
			extends Node
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	String	KIND_STR	= "binaryOperation";

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private BinaryOperationNode(Node            parent,
										BinaryOperation binaryOperation)
			{
				super(parent);
				this.binaryOperation = binaryOperation;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;

				return (obj instanceof BinaryOperationNode other) && (binaryOperation == other.binaryOperation)
						&& super.equals(other);
			}

			//----------------------------------------------------------

			@Override
			public int hashCode()
			{
				return 31 * super.hashCode() + binaryOperation.ordinal();
			}

			//----------------------------------------------------------

			@Override
			protected String getKindString()
			{
				return KIND_STR;
			}

			//----------------------------------------------------------

			@Override
			protected String getValueString()
			{
				return binaryOperation.toString();
			}

			//----------------------------------------------------------

			@Override
			protected double evaluate(double x)
			{
				return binaryOperation.evaluate(leftChild.evaluate(x), rightChild.evaluate(x));
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	BinaryOperation	binaryOperation;

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Node()
		{
		}

		//--------------------------------------------------------------

		private Node(Node parent)
		{
			this.parent = parent;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;

			return (obj instanceof Node other) && Objects.equals(leftChild, other.leftChild)
					&& Objects.equals(rightChild, other.rightChild);
		}

		//--------------------------------------------------------------

		@Override
		public int hashCode()
		{
			int code = Objects.hashCode(leftChild);
			code = 31 * code + Objects.hashCode(rightChild);
			return code;
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return toNodeString(0);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		protected boolean isEmpty()
		{
			return (leftChild == null) && (rightChild == null);
		}

		//--------------------------------------------------------------

		protected boolean isTerminal()
		{
			return false;
		}

		//--------------------------------------------------------------

		protected String getKindString()
		{
			return null;
		}

		//--------------------------------------------------------------

		protected String getValueString()
		{
			return null;
		}

		//--------------------------------------------------------------

		protected boolean addChild(Node node)
		{
			if (leftChild == null)
			{
				leftChild = node;
				return true;
			}
			if (rightChild == null)
			{
				rightChild = node;
				return true;
			}
			return false;
		}

		//--------------------------------------------------------------

		protected Node addChild(BinaryOperation operation)
		{
			Node node = new BinaryOperationNode(this, operation);
			if (parent == null)
			{
				node.leftChild = leftChild;
				leftChild = node;
			}
			else
			{
				node.leftChild = rightChild;
				rightChild = node;
			}
			node.leftChild.parent = node;
			return node;
		}

		//--------------------------------------------------------------

		protected double evaluate(double x)
		{
			return Double.NaN;
		}

		//--------------------------------------------------------------

		private Node getBinaryOperationAncestor(int precedence)
		{
			Node node = this;
			while (node.parent != null)
			{
				node = node.parent;
				if ((node instanceof BinaryOperationNode) &&
					 (((BinaryOperationNode)node).binaryOperation.precedence < precedence))
					break;
			}
			return node;
		}

		//--------------------------------------------------------------

		private String toNodeString(int level)
		{
			String str = getKindString();
			if (str == null)
				str = "";
			else
				str += " : " + getValueString();

			String indent = " ".repeat(level * 4);
			if (leftChild != null)
				str += "\n" + indent + "<L> " + leftChild.toNodeString(level + 1);
			if (rightChild != null)
				str += "\n" + indent + "<R> " + rightChild.toNodeString(level + 1);

			return str;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		protected	Node	parent;
		protected	Node	leftChild;
		protected	Node	rightChild;

	}

	//==================================================================


	// SYNTAX ERROR CLASS


	private static class SyntaxError
		extends Expression.Exception
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	SYNTAX_ERROR_STR	= "Syntax error in expression";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SyntaxError(ErrorId id,
							int     offset)
		{
			super(id, offset);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getPrefix()
		{
			return (SYNTAX_ERROR_STR + ": ");
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// PARSER ERROR CLASS


	/**
	 * A parser error indicates an internal problem with the parser: if the parser performs correctly, a
	 * parser error should never be thrown.
	 */

	private static class ParserError
		extends Expression.Exception
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	PARSER_ERROR_STR	= "Parser error";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ParserError(int errorNum,
							int offset)
		{
			super(PARSER_ERROR_STR + ' ' + errorNum, offset);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Expression(String str)
		throws Expression.Exception
	{
		this.str = str;
		parse(str);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	private static boolean isWhitespace(char ch)
	{
		final	String	WHITESPACE_CHARS	= " \t\n\r";

		return (WHITESPACE_CHARS.indexOf(ch) >= 0);
	}

	//------------------------------------------------------------------

	private static boolean isNumber(char ch)
	{
		return (((ch >= '0') && (ch <= '9')) || (ch == '.'));
	}

	//------------------------------------------------------------------

	private static boolean isAlpha(char ch)
	{
		return ((ch >= 'a') && (ch <= 'z'));
	}

	//------------------------------------------------------------------

	private static boolean isSymbol(char ch)
	{
		return (Symbol.forKey(ch) != null);
	}

	//------------------------------------------------------------------

	private static LexState getLexState(char ch)
	{
		if (ch == 0)
			return LexState.EOL;

		if (isWhitespace(ch))
			return LexState.WHITESPACE;

		if (isNumber(ch))
			return LexState.NUMBER;

		if (isAlpha(ch))
			return LexState.ALPHA;

		if (isSymbol(ch))
			return LexState.SYMBOL;

		return LexState.INVALID;
	}

	//------------------------------------------------------------------

	private static Token numberToToken(String str,
									   int    offset)
		throws Expression.Exception
	{
		try
		{
			return new Token.NumberToken(offset, Double.parseDouble(str));
		}
		catch (NumberFormatException e)
		{
			throw new Expression.Exception(ErrorId.INVALID_NUMBER, str, offset);
		}
	}

	//------------------------------------------------------------------

	private static Token alphaToToken(String str,
									  int    offset)
		throws Expression.Exception
	{
		if (str.equals(VARIABLE_STR))
			return new Token.VariableToken(offset);
		Keyword keyword = Keyword.forKey(str);
		if (keyword == null)
			throw new Expression.Exception(ErrorId.UNRECOGNISED_TOKEN, str, offset);
		return new Token.KeywordToken(offset, keyword);
	}

	//------------------------------------------------------------------

	private static Token symbolToToken(String str,
									   int    offset)
		throws Expression.Exception
	{
		if (str.length() > 1)
			throw new Expression.Exception(ErrorId.UNRECOGNISED_TOKEN, str, offset);
		return new Token.SymbolToken(offset, Symbol.forKey(str.charAt(0)));
	}

	//------------------------------------------------------------------

	private static List<Token> toTokens(String str)
		throws Expression.Exception
	{
		List<Token> tokens = new ArrayList<>();
		StringBuilder buffer = new StringBuilder();
		int charIndex = 0;
		int offset = 0;
		NumericState numericState = NumericState.SIGNIFICAND;
		LexState state = LexState.START;
		while (state != LexState.DONE)
		{
			char ch = (charIndex >= str.length()) ? 0 : str.charAt(charIndex);
			switch (state)
			{
				case START:
					state = getLexState(ch);
					break;

				case WHITESPACE:
					if (isWhitespace(ch))
					{
						++charIndex;
						break;
					}
					state = getLexState(ch);
					if (state != LexState.INVALID)
						offset = charIndex;
					break;

				case NUMBER:
				{
					boolean valid = false;
					switch (numericState)
					{
						case SIGNIFICAND:
							if (isNumber(ch))
								valid = true;
							else
							{
								if ((ch == 'E') || (ch == 'e'))
								{
									valid = true;
									numericState = NumericState.EXP_INDICATOR;
								}
							}
							break;

						case EXP_INDICATOR:
							if (isNumber(ch) || (ch == '+') || (ch == '-'))
								valid = true;
							numericState = NumericState.EXPONENT;
							break;

						case EXPONENT:
							if (isNumber(ch))
								valid = true;
							break;
					}
					if (valid)
					{
						buffer.append(ch);
						++charIndex;
						break;
					}
					numericState = NumericState.SIGNIFICAND;
					state = getLexState(ch);
					if (state != LexState.INVALID)
					{
						tokens.add(numberToToken(buffer.toString(), offset));
						buffer.setLength(0);
						offset = charIndex;
					}
					break;
				}

				case ALPHA:
					if (isAlpha(ch))
					{
						buffer.append(ch);
						++charIndex;
						break;
					}
					state = getLexState(ch);
					if (state != LexState.INVALID)
					{
						tokens.add(alphaToToken(buffer.toString(), offset));
						buffer.setLength(0);
						offset = charIndex;
					}
					break;

				case SYMBOL:
					if (isSymbol(ch))
					{
						if (!buffer.isEmpty())
						{
							tokens.add(symbolToToken(buffer.toString(), offset));
							buffer.setLength(0);
							offset = charIndex;
						}
						buffer.append(ch);
						++charIndex;
						break;
					}
					state = getLexState(ch);
					if (state != LexState.INVALID)
					{
						tokens.add(symbolToToken(buffer.toString(), offset));
						buffer.setLength(0);
						offset = charIndex;
					}
					break;

				case EOL:
					tokens.add(new Token.EofToken(offset));
					state = LexState.DONE;
					break;

				case INVALID:
					throw new Expression.Exception(ErrorId.CHARACTER_NOT_ALLOWED, Character.toString(ch), charIndex);

				case DONE:
					// do nothing
					break;
			}
		}

		return tokens;
	}

	//------------------------------------------------------------------

	private static Node parse(List<Token> tokens)
		throws Expression.Exception
	{
		Node tree = new Node();
		createAbstractSyntaxTree(tokens, 0, tree, false);
		return tree.leftChild;
	}

	//------------------------------------------------------------------

	private static int createAbstractSyntaxTree(List<Token> tokens,
												int         tokenIndex,
												Node        tree,
												boolean     subexpression)
		throws Expression.Exception
	{
		Node activeNode = tree;
		ParseState state = ParseState.OPERAND;
		while (state != ParseState.DONE)
		{
			Token token = tokens.get(tokenIndex++);
			switch (state)
			{
				case OPERAND:
				{
					// Test for terminal node
					if (activeNode.isTerminal())
						throw new ParserError(1, token.offset);

					// Number token
					if (token instanceof Token.NumberToken)
					{
						Node node = new Node.ConstantNode(activeNode, ((Token.NumberToken)token).value);
						if (!activeNode.addChild(node))
							throw new ParserError(2, token.offset);
						activeNode = node;
						state = ParseState.OPERATION;
					}

					// Variable token
					else if (token instanceof Token.VariableToken)
					{
						Node node = new Node.VariableNode(activeNode);
						if (!activeNode.addChild(node))
							throw new ParserError(3, token.offset);
						activeNode = node;
						state = ParseState.OPERATION;
					}

					// Keyword token
					else if (token instanceof Token.KeywordToken)
					{
						Keyword keyword = ((Token.KeywordToken)token).keyword;
						UnaryOperation operation = keyword.operation;
						if (operation == null)
						{
							double value = 0.0;
							if (keyword == Keyword.E)
								value = Math.E;
							else if (keyword == Keyword.PI)
								value = Math.PI;

							Node node = new Node.ConstantNode(activeNode, value);
							if (!activeNode.addChild(node))
								throw new ParserError(4, token.offset);
							activeNode = node;
							state = ParseState.OPERATION;
						}
						else
						{
							Node node = new Node.UnaryOperationNode(activeNode, operation);
							if (!activeNode.addChild(node))
								throw new ParserError(5, token.offset);
							activeNode = node;
						}
					}

					// Symbol token
					else if (token instanceof Token.SymbolToken)
					{
						Symbol symbol = ((Token.SymbolToken)token).symbol;
						if (symbol == Symbol.OPENING_PARENTHESIS)
						{
							Node subtree = new Node();
							tokenIndex = createAbstractSyntaxTree(tokens, tokenIndex, subtree, true);
							if (subtree.isEmpty())
								throw new SyntaxError(ErrorId.OPERAND_EXPECTED, token.offset);
							subtree = subtree.leftChild;
							if (!activeNode.addChild(subtree))
								throw new ParserError(6, token.offset);
							subtree.parent = activeNode;
							activeNode = subtree;
							state = ParseState.OPERATION;
						}
						else
						{
							UnaryOperation operation = symbol.unaryOperation;
							if (operation == null)
								throw new SyntaxError(ErrorId.OPERAND_EXPECTED, token.offset);
							Node node = new Node.UnaryOperationNode(activeNode, operation);
							if (!activeNode.addChild(node))
								throw new ParserError(7, token.offset);
							activeNode = node;
						}
					}

					// EOF token
					else if (token instanceof Token.EofToken)
						throw new SyntaxError(ErrorId.OPERAND_EXPECTED, token.offset);

					break;
				}

				case OPERATION:
				{
					// Number token, variable token or keyword token
					if ((token instanceof Token.NumberToken) ||
						 (token instanceof Token.VariableToken) ||
						 (token instanceof Token.KeywordToken))
						throw new SyntaxError(ErrorId.BINARY_OPERATION_EXPECTED, token.offset);

					// Symbol token
					if (token instanceof Token.SymbolToken)
					{
						Symbol symbol = ((Token.SymbolToken)token).symbol;
						if (symbol == Symbol.OPENING_PARENTHESIS)
							throw new SyntaxError(ErrorId.BINARY_OPERATION_EXPECTED, token.offset);
						if (symbol == Symbol.CLOSING_PARENTHESIS)
						{
							if (!subexpression)
								throw new SyntaxError(ErrorId.UNEXPECTED_CLOSING_PARENTHESIS,
													  token.offset);
							state = ParseState.DONE;
						}
						else
						{
							BinaryOperation operation = symbol.binaryOperation;
							if (operation == null)
								throw new SyntaxError(ErrorId.BINARY_OPERATION_EXPECTED, token.offset);
							activeNode = activeNode.getBinaryOperationAncestor(operation.precedence).
																					addChild(operation);
							state = ParseState.OPERAND;
						}
					}

					// EOF token
					else if (token instanceof Token.EofToken)
					{
						if (subexpression)
							throw new SyntaxError(ErrorId.CLOSING_PARENTHESIS_EXPECTED, token.offset);
						state = ParseState.DONE;
					}

					break;
				}

				case DONE:
					// do nothing
					break;
			}
		}

		return tokenIndex;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		return (obj instanceof Expression other) && tree.equals(other.tree);
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		return tree.hashCode();
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		return str;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public double evaluate(double x)
	{
		return tree.evaluate(x);
	}

	//------------------------------------------------------------------

	public String toCanonicalString()
	{
		StringBuilder buffer = new StringBuilder(256);
		if (tokens != null)
		{
			for (int i = 0; i < tokens.size(); i++)
			{
				Token token = tokens.get(i);
				if (token instanceof Token.EofToken)
					break;
				if (i > 0)
					buffer.append(' ');
				buffer.append(token);
			}
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

	private void parse(String str)
		throws Expression.Exception
	{
		tokens = toTokens(str);
		tree = parse(tokens);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String		str;
	private	List<Token>	tokens;
	private	Node		tree;

}

//----------------------------------------------------------------------
