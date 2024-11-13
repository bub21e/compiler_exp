package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.ir.InstructionKind;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {

    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息

        for(Instruction instruction : originInstructions) {
    
            InstructionKind kind = instruction.getKind();

            // 排除 UnaryOp
            if(kind.isUnary()) {
                this.instructions.add(instruction);
                //处理 return
                if(kind.isReturn()) {
                    return;
                }               
                continue;
            } 

            // 排除 左操作数不是立即数
            if(instruction.getLHS().isIRVariable()) {
                this.instructions.add(instruction);
                continue;
            }   

            // 以下左操作数都是立即数
            // 合并 右操作数也是立即数
            if(instruction.getRHS().isImmediate()) {
                
                IRImmediate newValue;

                IRImmediate leftValue = (IRImmediate)instruction.getRHS();
                IRImmediate rightValue = (IRImmediate)instruction.getRHS();
                
                // 获取新值
                int value = switch (instruction.getKind()) {
                    case ADD -> {
                        yield leftValue.getValue() + rightValue.getValue();
                    }
                    case SUB -> {
                        yield leftValue.getValue() - rightValue.getValue();
                    }
                    case MUL -> {
                        yield leftValue.getValue() * rightValue.getValue();
                    }
                    default -> {
                        System.out.println("error occured loading IR.");
                        yield 0;
                    }
                };

                newValue = IRImmediate.of(value);
                instructions.add(Instruction.createMov(instruction.getResult(),newValue));
                
                continue;
            }

            // 右操作数不是立即数
            switch (kind) {
                
                // 调整 立即数乘法和左立即数减法
                // 用寄存器代替立即数
                case MUL -> {
                    IRVariable temp = IRVariable.temp();
                    instructions.add(Instruction.createMov(temp, instruction.getLHS()));
                    instructions.add(Instruction.createMul(instruction.getResult(), temp, instruction.getRHS()));
                }
                case SUB -> {
                    IRVariable temp = IRVariable.temp();
                    instructions.add(Instruction.createMov(temp, instruction.getLHS()));
                    instructions.add(Instruction.createSub(instruction.getResult(), temp, instruction.getRHS()));
                }
                // 调整 一般左立即数
                case ADD -> {
                    instructions.add(Instruction.createAdd(instruction.getResult(), instruction.getRHS(), instruction.getLHS()));
                }
                default -> {
                    System.out.println("error occured loading IR.");
                }
            }
        }
    }

    // 处理 寄存器分配 和 栈的调度
    public Integer getReg() {
        
    }

    public void setReg(Integer key, String value) {

    }

    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
        
        // 获取变量待读取次数
        for (Instruction instruction : instructions) {
            List<IRValue> values = instruction.getOperands();
            for(IRValue val : values) {
                if (val.isIRVariable()) {
                    IRVariable value = (IRVariable) val;
                    referenceOfVar.put(value.getName(),referenceOfVar.getOrDefault(value.getName(), 0) + 1);
                }
            }
        }

        // 初始化寄存器链表
        for(int i = 0 ; i <=6 ; i++){
            regList.add(i);
        }

        // 从 Instructions 里读取中间代码，生成汇编代码字符串输入 assemblyOutput ,同时管理 regMap 映射关系
        for (Instruction instruction : instructions) 
        {   
            StringBuffer builder = new StringBuffer();
            switch (instruction.getKind()) {
                case MOV -> {

                    if(instruction.getFrom().isImmediate()) {

                        // 获取 result 的寄存器
                        String resultName = ((IRVariable) instruction.getResult()).getName();
                        Integer reg = regMap.getByValue(resultName);

                        // 处理写寄存器获取失败
                        if (reg == null) {
                            reg = getReg();
                            // 维护 regMap
                            setReg(reg, resultName);
                        }
                        
                        builder.append("li t" + reg + ", " + ((IRImmediate) instruction.getFrom()).getValue());
                        referencedVar.add(resultName);

                    } else {

                        // 获取 instruction 中 result 和 from 的寄存器
                        String resutlName = ((IRVariable) instruction.getResult()).getName(), 
                            fromName = ((IRVariable) instruction.getFrom()).getName();

                        Integer leftReg = regMap.getByValue(resutlName), 
                            rightReg = regMap.getByValue(fromName);
                        
                        if (leftReg == null) {
                            leftReg = getReg();
                            setReg(rightReg, resutlName);
                        }
                        // 从堆栈中取值
                        if (referencedVar.contains(fromName) && rightReg == null) {

                        }

                        // 读维护
                        regList.push(regList.remove(regList.indexOf(rightReg)));
                        referenceOfVar.put(fromName,referenceOfVar.getOrDefault(fromName,0)-1);

                        builder.append("addi t" + leftReg + ", t" + rightReg + ", 0");
                        referencedVar.add(resutlName);
                    }

                    assemblyOutput.add(builder.toString());

                }
                case ADD -> {

                } 
                case SUB -> {

                }
                case MUL -> {

                }
                case RET -> {

                    if(instruction.getReturnValue().isImmediate()) {

                        builder.append("li a0, " + ((IRImmediate) instruction.getReturnValue()).getValue());
                    } else {
                        Integer reg = regMap.getByValue(((IRVariable) instruction.getReturnValue()).getName());
                        //

                        builder.append("addi a0, t" + reg + ", 0");
                    }

                    assemblyOutput.add(builder.toString());
                    assemblyOutput.add("ret");
                    
                }
                default -> {
                    System.out.println("error occured running.");
                }
            }
        }
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter((path)))){ // try-with-resources
            for(String output : assemblyOutput) {
                writer.write(output);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    List<Instruction> instructions = new ArrayList<>(); // 预处理后的 instruction

    BMap<Integer, String> regMap = new BMap<>(); // 寄存器 映射关系:getReg时维护
    LinkedList<Integer> regList = new LinkedList<>(); // 寄存器 链表:读时维护
    
    Map<String, Integer> referenceOfVar = new HashMap<>(); // 变量 待读取次数：读时维护
    HashSet<String> referencedVar = new HashSet<>(); // 变量 已被赋值：写时维护

    List<String> assemblyOutput = new ArrayList<>(); // 生成的汇编代码
}

