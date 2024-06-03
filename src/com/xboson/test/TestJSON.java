////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月2日 下午1:38:30
// 原始文件路径: xBoson/src/com/xboson/test/TestJSON.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.xboson.been.ResponseRoot;
import com.xboson.j2ee.container.XResponse;
import com.xboson.util.OutputStreamSinkWarp;
import com.xboson.util.StringBufferOutputStream;
import com.xboson.util.Tool;


public class TestJSON extends Test {


	public void test() throws IOException {
		been_to_json();
		outputstream_warp();
		speed();
		thread_safe();
		map();
	}


	private static JsonAdapter<TestData> jsonAdapter;

	static {
		Moshi moshi = new Moshi.Builder().build();
		jsonAdapter = moshi.adapter(TestData.class);
	}


	public void map() {
	  sub("LinkedHashMap");
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("a", 1);
    JsonAdapter ja = Tool.getAdapter(map.getClass());
    msg(ja.toJson(map));
    success("LinkedHashMap Worked !");
  }


	public void speed() {
		int count = 100000;
		TestData data = new TestData();

		{
			Moshi moshi = new Moshi.Builder().build();
			beginTime();
			for (int i=0; i<count; ++i) {
				jsonAdapter = moshi.adapter(TestData.class);
				jsonAdapter.toJson(data);
			}
			endTime("cache Moshi"); // 100000 Used Time 218ms
		}

		{
			Moshi moshi = new Moshi.Builder().build();
			jsonAdapter = moshi.adapter(TestData.class);
			beginTime();
			for (int i=0; i<count; ++i) {
				jsonAdapter.toJson(data);
			}
			endTime("cache adapter"); // 100000 Used Time 156ms
		}

		{
			beginTime();
			for (int i=0; i<count; ++i) {
				Moshi moshi = new Moshi.Builder().build();
				jsonAdapter = moshi.adapter(TestData.class);
				jsonAdapter.toJson(data);
			}
			endTime("All Function"); // 100000 Used Time 765ms
		}

		success("time test");
	}


	public void thread_safe() {
		final TestData data = new TestData();
		data.change();
		final Moshi moshi = new Moshi.Builder().build();
		final JsonAdapter<TestData> jsonAdapter2 = moshi.adapter(TestData.class);
		final String b = jsonAdapter2.toJson(data);

		final int count = 300000;
		final int threadc = 10;
		Thread t[] = new Thread[threadc];

		for (int c = 0; c<threadc; ++c) {
			t[c] = new Thread(new Runnable() {
				public void run() {
					for (int i=0; i<count; ++i) {
						jsonAdapter = moshi.adapter(TestData.class);
						String a = jsonAdapter.toJson(data);
						if (! a.equals(b)) {
							fail("bad value \n" + a + "\n" + b);
							System.exit(1);
						}
					}
					msg("Thread safe over " + Thread.currentThread().getId());
				}
			});
			t[c].start();
		}

		for (int c = 0; c<threadc; ++c) {
			try {
				t[c].join();
			} catch (InterruptedException e) {
				fail(c, t[c], "interrupted");
			}
		}
		success("thread safe");
	}


	public void been_to_json() throws IOException {
		XResponse ret = new XResponse();

		TestData src = new TestData();
		src.change();
		ret.setData(src);
		msg(ret.toJSON());
		eq(ret.toJSON(), ret.toJSON(), "eq");

		success("been to JSON");
	}


	public void outputstream_warp() throws IOException {
		TestData data = new TestData();
		data.change();

		OutputStream out = new StringBufferOutputStream();
		jsonAdapter.toJson(new OutputStreamSinkWarp(out), data);

		String a = out.toString();
		String b = jsonAdapter.toJson(data);

		TestData aa = jsonAdapter.fromJson(a);
		TestData bb = jsonAdapter.fromJson(b);

		msg(a);
		eq(aa, bb, "from json");
		eq(aa, data, "eq source");
	}


	public static void main(String[] s) throws Exception {
		new TestJSON();
	}
}