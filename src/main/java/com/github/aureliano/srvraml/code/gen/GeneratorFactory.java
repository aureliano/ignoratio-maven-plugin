package com.github.aureliano.srvraml.code.gen;

public final class GeneratorFactory {

	public static ICodeGenerator createGenerator(GeneratorType type) {
		if (type == null) {
			return null;
		}
		
		switch (type) {
			case MODEL : return new ModelGenerator();
			case SERVICE : return new ServiceGenerator();
			default : return null;
		}
	}
}