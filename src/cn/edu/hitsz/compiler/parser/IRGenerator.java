package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.ir.IRImmediate;
// import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        tokens.push(currentToken);
        // throw new NotImplementedException();
    }

    private IRValue tempcreat(String kind,String name) {
        IRValue temp;
        if("IntConst".equals(kind)){
            temp = IRImmediate.of(Integer.parseInt(name));
        }else {
            temp = IRVariable.named(name);
        }
        return temp;
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        switch(production.index()) {
            case 6 ->{// =
                Token token = tokens.pop();
                IRVariable value;
                IRValue num;

                num = tempcreat(token.getKindId(),token.getText());
                tokens.pop();

                token = tokens.pop();
                
                value = IRVariable.named(token.getText());
                
                IRList.add(Instruction.createMov(value, num));
                break;
            }
            case 7->{// return
                Token token = tokens.pop();
                IRValue value;

                value = tempcreat(token.getKindId(),token.getText());
                
                IRList.add(Instruction.createRet(value));
                break;
            }
            case 8->{// +
                Token token = tokens.pop();
                IRValue num1,num2;

                num1 = tempcreat(token.getKindId(),token.getText());

                tokens.pop();

                token = tokens.pop();
                num2 = tempcreat(token.getKindId(),token.getText());
                IRVariable temp = IRVariable.temp();
                tokens.push(Token.normal("id", temp.getName()));
                IRList.add(Instruction.createAdd(temp, num2, num1));

                break;
            }
            case 9->{// -
                Token token = tokens.pop();
                IRValue num1,num2;

                num1 = tempcreat(token.getKindId(),token.getText());

                tokens.pop();

                token = tokens.pop();
                num2 = tempcreat(token.getKindId(),token.getText());
                IRVariable temp = IRVariable.temp();
                tokens.push(Token.normal("id", temp.getName()));
                IRList.add(Instruction.createSub(temp, num2, num1));

                break;
            }
            case 11->{// *
                Token token = tokens.pop();
                IRValue num1,num2;

                num1 = tempcreat(token.getKindId(),token.getText());

                tokens.pop();

                token = tokens.pop();
                num2 = tempcreat(token.getKindId(),token.getText());
                IRVariable temp = IRVariable.temp();
                tokens.push(Token.normal("id", temp.getName()));
                IRList.add(Instruction.createMul(temp, num2, num1));
                
                break;
            }
        }
        switch(production.index()){
            case 2:
            case 3:
            case 4:
            case 5:
                tokens.pop();
                break;
            case 13:
                tokens.pop();
                Token token = tokens.pop();
                tokens.pop();
                tokens.push(token);
                break;
            default:
                break;
        }
    }

    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
        // throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        this.table = table;
        this.tokens = new Stack<>();
        this.IRList = new ArrayList<>();
        // throw new NotImplementedException();
    }

    public List<Instruction> getIR() {
        // TODO
        return IRList;
        // throw new NotImplementedException();
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }

    SymbolTable table;
    private Stack<Token> tokens;
    private List<Instruction> IRList;
}

