package ts.trans.factory;

import ts.trans.Translator;

public interface TFactory {
	Translator get(String id);
}
