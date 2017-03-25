package ts.trans.impl;

import ts.http.HttpParams;
import ts.http.HttpPostParams;
import ts.trans.AbstractOnlineTranslator;
import ts.trans.LANG;
import ts.trans.annotation.TranslatorComponent;

import net.sf.json.JSONObject;

@TranslatorComponent(id = "jinshan")
final public class JinshanTranslator extends AbstractOnlineTranslator {
	
	public JinshanTranslator(){
		langMap.put(LANG.EN, "en");
		langMap.put(LANG.ZH, "zh");
	}
	
	@Override
	protected String getResponse(LANG from, LANG targ, String query) throws Exception{
		HttpParams params = new HttpPostParams()
				.put("f", langMap.get(from))
				.put("t", langMap.get(targ))
				.put("w", query);
		
		return params.send2String("http://fy.iciba.com/ajax.php?a=fy");
	}
	
	
	@Override
	protected String parseString(String jsonString){
		JSONObject jsonObject = JSONObject.fromObject(jsonString);
		String result = jsonObject.getJSONObject("content").getString("out");
		return result;
	}
}
