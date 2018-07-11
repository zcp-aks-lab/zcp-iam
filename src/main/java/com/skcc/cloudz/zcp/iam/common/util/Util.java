package com.skcc.cloudz.zcp.iam.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class Util {

	public static <T> T toJson(String filename) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<T> typeReference = new TypeReference<T>() {
		};
		InputStream inputStream = TypeReference.class.getResourceAsStream(filename);
		return mapper.readValue(inputStream, typeReference);
	}

	public static <T> List<T> asList(T t) {
		List<T> list = new ArrayList<T>();
		list.add(t);
		return list;
	}

	public static String asCommaData(List<String> datas) {
		String commaData = "";
		int i = 0;
		for (String data : datas) {
			if (i != 0)
				commaData = ",";
			commaData += data;
			i++;
		}
		return commaData;
	}

	public static int compare(double a, double b) {
		if (a == b)
			return 0;
		if (a > b)
			return 1;
		else
			return -1;
	}

	public static int compare(int a, int b) {
		if (a == b)
			return 0;
		if (a > b)
			return 1;
		else
			return -1;
	}

	public static List<String> MapToList(Map<String, String> data) {
		List<String> datas = new ArrayList<>();
		if (data != null)
			for (String key : data.keySet()) {
				String strLabel = key + "=" + data.get(key);
				datas.add(strLabel);
			}
		return datas;
	}
}
