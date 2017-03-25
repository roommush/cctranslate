package ts.trans.factory;

import java.net.URISyntaxException;

import ts.trans.Translator;
import ts.trans.exception.DupIdException;

final public class TranslatorFactory extends AbstractTranslatorFactory{

	public TranslatorFactory() throws ClassNotFoundException, InstantiationException, IllegalAccessException, DupIdException, URISyntaxException {
		super();
	}

	@Override
	public Translator get(String id) {
		return translatorMap.get(id);
	}

}
