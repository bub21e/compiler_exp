package cn.edu.hitsz.compiler.parser;

// import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.NonTerminal;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Term;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.symtab.SymbolTableEntry;

import java.util.Stack;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {

    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作

        // throw new NotImplementedException();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        if(production.index() == 5) {
            Symbol head = new Symbol(production.head());
            Token token = tokens.pop();
            if(token.getKind().getCode() == 1){
                types.put(head.toString(), SourceCodeType.Int);
            }
        }
        else if(production.index() == 4) {
            List<Term> body = production.body();
            Symbol head = new Symbol((NonTerminal)body.get(0));
            Token id = tokens.pop();
            SourceCodeType type = types.get(head.toString());
            String idtext = id.getText();
            if(table.has(idtext)) {
                SymbolTableEntry entry = table.get(idtext);
                entry.setType(type);
            }
        }
        // throw new NotImplementedException();
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
        tokens.add(currentToken);
        // throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        this.table = table;
        this.tokens = new Stack<>();
        this.types = new HashMap<>();
        // throw new NotImplementedException();
    }

    private SymbolTable table;
    private Map<String,SourceCodeType> types;
    private Stack<Token> tokens;

}

