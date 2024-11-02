package cn.edu.hitsz.compiler.lexer;

//import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.stream.StreamSupport;
import java.util.ArrayList;
import java.util.List;



/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private List<Token> tokens;

    private String originText;
    private int currentPosition;
    private char currentChar;

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.tokens = new ArrayList<>();
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        originText = FileUtils.readFile(path);
        currentPosition = 0;
        //throw new NotImplementedException();
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
        if(originText == null){
            throw new IllegalStateException("File has not been loaded!");
        }
        
        tokens.clear();
        currentPosition = 0;
        while(currentPosition < originText.length()){
            currentChar = getChar();
            while(currentChar == '\s' || currentChar == '\t' || currentChar == '\n' || currentChar == ' ' || currentChar == '\r'){
                currentChar = getChar();
            }
            int lexemeBegin = currentPosition - 1;

            if(Character.isLetter(currentChar)){
                do{
                    currentChar = getChar();
                }while(Character.isLetterOrDigit(currentChar));
                currentPosition--;
                String sToken = originText.substring(lexemeBegin,currentPosition);
                //System.out.println(sToken);
                tokens.add(getToken(sToken));
            }else if(Character.isDigit((currentChar))){
                do{
                    currentChar = getChar();
                }while(Character.isDigit(currentChar));
                currentPosition--;
                String sToken = originText.substring(lexemeBegin,currentPosition);
                //System.out.println(sToken);
                tokens.add(Token.normal("IntConst",sToken));
            }else{
                //System.out.println(currentChar);
                switch(currentChar){
                    case '+':
                        tokens.add(Token.simple("+")); 
                        break;
                    case '-':
                        tokens.add(Token.simple("-")); 
                        break;
                    case '*':
                        tokens.add(Token.simple("*"));     
                        break;
                    case '/':
                        tokens.add(Token.simple("/"));
                        break;
                    case '=':
                        tokens.add(Token.simple("="));
                        break;
                    case ',':
                        tokens.add(Token.simple(",")); 
                        break;
                    case ';':
                        tokens.add(Token.simple("Semicolon"));
                        break;
                    case '(':
                        tokens.add(Token.simple("("));  
                        break;                     
                    case ')':
                        tokens.add(Token.simple(")")); 
                        break;      
                }
            }
        }
        tokens.add(Token.eof());
        //throw new NotImplementedException();
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        return tokens;
        //throw new NotImplementedException();
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }
    private char getChar(){
        if(currentPosition < originText.length()){
            return originText.charAt((currentPosition++));
        }
        return '\0';
    }

    private Token getToken(String token){
        switch(token){
            case "int":
                return Token.simple("int");
            case "return":
                return Token.simple("return");
            default:
                if (!symbolTable.has(token)) {
                    symbolTable.add(token);   
                }
                return Token.normal("id",token);
        }
    }
}
