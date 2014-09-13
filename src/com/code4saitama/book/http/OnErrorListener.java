package com.code4saitama.book.http;

/**
 * エラー発生時のコールバック用インターフェース
 * @author makiuchi
 *
 */
public interface OnErrorListener {
	public void raise(Exception e);
}
