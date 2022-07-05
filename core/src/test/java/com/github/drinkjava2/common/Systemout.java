package com.github.drinkjava2.common;

/**
 * Debug Util
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
@SuppressWarnings("all")
public class Systemout {
	private static boolean allowPrint = false; //开发期打开好显示输出内容，提交时关闭以减少命令行输出

	public static boolean isAllowPrint() {
		return allowPrint;
	}

	public static void setAllowPrint(boolean allowPrint) {
		Systemout.allowPrint = allowPrint;
	}

	public static void print(Object obj) {
		if (allowPrint)
			System.out.print(obj);
	}

	public static void println(Object obj) {
		if (allowPrint)
			System.out.println(obj);
	}

	public static void println() {
		if (allowPrint)
			System.out.println();
	}

}
