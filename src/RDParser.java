public class RDParser {
	char[] input;
	int parsingPosition;
	boolean[] isLeftAssociative;
	boolean[] isBinary;
	boolean[] isOperator;
	int[] presedence;
	public RDParser() {
		isLeftAssociative=new boolean[256];
		isBinary=new boolean[256];
		isOperator=new boolean[256];
		presedence= new int[256];
		isOperator['+']=true;
		isOperator['-']=true;
		isOperator['*']=true;
		isOperator['/']=true;
		isOperator['^']=true;
		isOperator['&']=true;
		isOperator['|']=true;
		isOperator['~']=true;
		isLeftAssociative['+']=true;
		isLeftAssociative['-']=true;
		isLeftAssociative['*']=true;
		isLeftAssociative['/']=true;
		isLeftAssociative['^']=false;
		isLeftAssociative['&']=true;
		isLeftAssociative['|']=true;
		isLeftAssociative['~']=false;
		isBinary['+']=true;
		isBinary['-']=true;
		isBinary['*']=true;
		isBinary['/']=true;
		isBinary['^']=true;
		isBinary['&']=true;
		isBinary['|']=true;
		isBinary['~']=false;
		presedence['|']=0;
		presedence['&']=1;
		presedence['+']=2;
		presedence['-']=2;
		presedence['~']=2;
		presedence['*']=3;
		presedence['/']=3;
		presedence['^']=4;
	}

	private int Next() {
		if(parsingPosition<input.length)
			return input[parsingPosition];
		else
			return -1;
	}

	private boolean Expect(int e) {
		if(Next()==e) {
			Consume();
			return true;
			}
		else {
			System.out.println("Error: Expected '"+((char)e)+"'");
			return false;
		}
	}
	private void Consume() {
		if(parsingPosition<input.length) ++parsingPosition;
	}

	public void addVariable(String str, int value) {
	}

	private int symNumber(String ident) {
		int i=0;
		while( (i+parsingPosition<input.length) && (input[parsingPosition+i]>='0') &&(input[parsingPosition+i]<='9') ) {
			++i;
			}
		int n=0;
		int t=1;
		int p=parsingPosition+i;
		for(--i;i>=0; i--) {
			System.out.println(ident+"i="+i+" pp="+parsingPosition + " il="+input.length+" i+pp="+(parsingPosition+i)+" n="+n+" c="+(input[parsingPosition+i]));
			n=n+t*(input[parsingPosition+i]-'0');
			t=t*10;
			}
		//n=n+f*t;
		parsingPosition=p;
		System.out.println(ident+"symNumber="+n+" Next()="+((char)Next())+" pp="+parsingPosition);
		return n;
	}

	private int P(String ident) {
		System.out.println(ident+"P() Next()="+((char)Next()));
		if(isOperator[Next()]&&(!isBinary[Next()])) {
			int op=Next();
			Consume();
			switch(op) {
				case '~':
					return -Expr(presedence[op],ident+"  ");
				default:
					System.out.println(ident+"Unknown unary operator '"+((char)op)+"'");
					return -1;
			}
		}
		else if(Next()=='(') {
			Consume();
			System.out.println(ident+"parsing '(' next="+((char)Next()));
			int t=Expr(0, ident+"  ");
			System.out.println(ident+"expecting ')', t="+t+" next="+((char)Next()));
			if(Expect(')'))
				return t;
			else
				return -1;
		}
		else if( (Next()>='0') && (Next()<='9')) {
			int t=symNumber(ident+"  ");
			//Consume();
			return t;
		}
		else {
			System.out.println(ident+"Error: No case for P('"+((char)Next())+"')");
			return -1;
		}
	}

	private int Expr(int p, String ident) {
		System.out.println(ident+"Expr("+p+") Next()="+((char)Next()));
		int t1=P(ident+"  ");
		System.out.println(ident+"t1="+t1);
		while((Next()!=-1) &&isBinary[Next()] && presedence[Next()]>=p ) {
			System.out.println(ident+"Expr while Next()="+((char)Next()));
			int op = Next();
			Consume();
			int t2=0;
			if(isLeftAssociative[op])
				t2=Expr(presedence[op]+1, ident+"  ");
			else
				t2=Expr(presedence[op], ident+"  ");
			System.out.println(ident+"t2="+t2);
			System.out.println(ident+"op="+((char)op));
			switch(op) {
				case '+':
					System.out.println(ident+"t1="+t1+" t2="+t2+" t1+t2="+(t1+t2));
					return t1+t2;
				case '-':
					System.out.println(ident+"t1="+t1+" t2="+t2+" t1-t2="+(t1-t2));
					return t1-t2;
				case '*':
					System.out.println(ident+"t1="+t1+" t2="+t2+" t1*t2="+(t1*t2));
					return t1*t2;
				case '/':
					System.out.println(ident+"t1="+t1+" t2="+t2+" t1/t2="+(t1/t2));
					return t1/t2;
				case '^':
					System.out.println(ident+"t1="+t1+" t2="+t2+" t1^t2="+(int)Math.round(Math.pow(t1, t2)));
					return (int)Math.round(Math.pow(t1, t2));
				case '&':
					System.out.println(ident+"t1="+t1+" t2="+t2+" t1&t2="+(t1&t2));
					return t1&t2;
				case '|':
					System.out.println(ident+"t1="+t1+" t2="+t2+" t1|t2="+(t1|t2));
					return t1|t2;
				default:
					System.out.println(ident+"Unknown binary operator '"+((char)op)+"'");
					return -1;
				}
		}
		return t1;
	}

	public int Evaluate(String str) {
		input = str.toCharArray();
		int i=0;
		int j=1;
		while((j!=str.length()) && (i!=str.length()) ){
			if(input[i]==' ') {
				input[i]=input[j];
				input[j++]=' ';
				}
			else {++i;}
		}
		System.out.println(input);
		parsingPosition=0;
		return Expr(0, "");
	}

	public static void main( String[] args ) {
		RDParser parser=new RDParser();
		//System.out.println(parser.Evaluate("  -   2*(3+ 2^ (4-1  ) )"));
		System.out.println(parser.Evaluate("(12+34)*5"));
		System.out.println("--------------------------");
		System.out.println(parser.Evaluate("2^4"));
	}
}