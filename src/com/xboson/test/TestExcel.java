////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-1-7 下午2:34
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/test/TestExcel.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import com.xboson.util.StringBufferOutputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


public class TestExcel extends Test {


  @Override
  public void test() throws Throwable {
    testWrite();
  }


  public void testWrite() throws Exception{
    // 第一步 创建文档对象
    Workbook wb = new HSSFWorkbook();
    Sheet sheet = wb.createSheet("分区信息");
    Row headRow = sheet.createRow(0);
    String[] heads = {"省","市","区（县）","定区编码","关键字",
            "起始号","结束号","单双号","省市区编码"};
    for(int i=0;i<heads.length;i++){
      Cell cell = headRow.createCell(i);
      cell.setCellValue(heads[i]);
    }

    for(int i=1; i<10; ++i){
      //从第二行开始
      Row row = sheet.createRow(i);

      for(int j=0;j<9;j++){
        Cell cell = row.createCell(j);
        cell.setCellValue(i + " " + j);
      }
    }

    StringBufferOutputStream buf = new StringBufferOutputStream();
    wb.write(buf);

    TestTool.printArr(buf.toBytes());
  }


  public static void main(String[] a) {
    new TestExcel();
  }
}
