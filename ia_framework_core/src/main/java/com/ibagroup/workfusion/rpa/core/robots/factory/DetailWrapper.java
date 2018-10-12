package com.ibagroup.workfusion.rpa.core.robots.factory;

public abstract class DetailWrapper implements Wrapper {

	abstract void onstart(Object object);

	abstract void oncompletion(Object object);
}
