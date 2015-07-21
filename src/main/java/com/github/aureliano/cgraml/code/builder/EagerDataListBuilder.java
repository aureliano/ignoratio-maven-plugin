package com.github.aureliano.cgraml.code.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.github.aureliano.cgraml.code.gen.ServiceFetchInterfaceGenerator;
import com.github.aureliano.cgraml.code.meta.ClassMeta;
import com.github.aureliano.cgraml.code.meta.FieldMeta;
import com.github.aureliano.cgraml.code.meta.MethodMeta;
import com.github.aureliano.cgraml.code.meta.Visibility;
import com.github.aureliano.cgraml.helper.CodeBuilderHelper;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

public class EagerDataListBuilder implements IBuilder {

	private ClassMeta clazz;
	
	public EagerDataListBuilder() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public EagerDataListBuilder parse(String pkg, String entity, Object resource) {
		String javaDoc = new StringBuilder()
			.append("Generated by cgraml-maven-plugin.")
			.append("\n\n")
			.append("Eager data list which under iteration fetch data dynamically.\n")
			.append("WARNING! Despite it extends ArrayList you mustn't use it by yourself. If you keep on this you're on your own...")
			.toString();
		
		this.clazz = new ClassMeta()
			.withPackageName(pkg)
			.withJavaDoc(javaDoc)
			.withClassName(StringUtils.capitalize(entity));
		
		this.addSerialVersionNumberField();
		this.addLoggerField();
		this.addVirtualSizeField();
		this.addServiceField();
		
		this.addSizeMethod();
		this.addGetMethod();
	
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public EagerDataListBuilder build() {
		this.buildJavaClass();
		return this;
	}
	
	private void buildJavaClass() {
		try {
			JCodeModel codeModel = new JCodeModel();
			JDefinedClass definedClass = codeModel._class(this.clazz.getCanonicalClassName());
			definedClass.javadoc().append(this.clazz.getJavaDoc());
			
			JClass collectionType = codeModel.ref(ArrayList.class).narrow(codeModel.ref("E"));
			definedClass._extends(collectionType).generify("E");
			
			for (String interfaceName : this.clazz.getInterfaces()) {
				definedClass._implements(codeModel.ref(interfaceName));
			}
			
			JMethod constructor = definedClass.constructor(JMod.PUBLIC);
			JClass param = codeModel.ref(this.clazz.getPackageName() + ".service." + ServiceFetchInterfaceGenerator.CLASS_NAME).narrow(codeModel.ref("?"));
			constructor.param(param, "service");
			constructor.body().directStatement(this.getConstructoryBody());
			
			this.appendClassAttributes(codeModel, definedClass);
			this.appendClassMethods(codeModel, definedClass);
			
			codeModel.build(new File("src/main/java"));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private String getConstructoryBody() {
		return new StringBuilder()
			.append("logger.info(\"Fetching data from \" + this.service.getClass().getName() + \" service with 1 page(s) and starting page 0\");")
			.append("\n" + CodeBuilderHelper.tabulation(2))
			.append(this.clazz.getPackageName() + ".model.ICollectionModel<?> model = service.httpGet();")
			.append("\n\n" + CodeBuilderHelper.tabulation(2))
			.append("this.virtualSize = model.getSize();")
			.append("\n" + CodeBuilderHelper.tabulation(2))
			.append("this.service = service;")
			.append("\n" + CodeBuilderHelper.tabulation(2))
			.append("super.addAll((java.util.Collection<? extends E>) model.getElements());")
			.append("\n" + CodeBuilderHelper.tabulation(2))
			.toString();
	}

	private void appendClassMethods(JCodeModel codeModel, JDefinedClass definedClass) {
		for (MethodMeta method : this.clazz.getMethods()) {
			CodeBuilderHelper.addMethodToClass(codeModel, definedClass, method);
		}
	}

	private void appendClassAttributes(JCodeModel codeModel, JDefinedClass definedClass) {
		for (FieldMeta field : this.clazz.getFields()) {
			CodeBuilderHelper.addAttributeToClass(codeModel, definedClass, field);
		}
	}

	private void addSizeMethod() {
		MethodMeta method = new MethodMeta();
		
		method.setName("size");
		method.setVisibility(Visibility.PUBLIC);
		method.setReturnType("int");
		method.setBody("return this.virtualSize;");
		
		this.clazz.addMethod(method);
	}
	
	private void addGetMethod() {
		MethodMeta method = new MethodMeta();
		
		method.setName("get");
		method.setVisibility(Visibility.PUBLIC);
		method.setReturnType("E");
		method.setBody(this.getGetMethodBody());
		
		FieldMeta param = new FieldMeta();
		param.setName("index");
		param.setType("int");
		
		method.addParameter(param);
		this.clazz.addMethod(method);
	}

	private String getGetMethodBody() {
		return new StringBuilder()
			.append("if (index >= this.virtualSize) {")
			.append("\n" + CodeBuilderHelper.tabulation(3))
			.append("throw new IndexOutOfBoundsException(\"Index: \" + index + \", Size: \" + this.virtualSize);")
			.append("\n" + CodeBuilderHelper.tabulation(2))
			.append("}")
			.append("\n\n" + CodeBuilderHelper.tabulation(2))
			.append("if (index >= super.size()) {")
			.append("\n" + CodeBuilderHelper.tabulation(3))
			.append(this.clazz.getPackageName() + ".parameters.IServiceParameters params = service.getParameters();")
			.append("\n" + CodeBuilderHelper.tabulation(3))
			.append("params.withPages(1).withStart(super.size());")
			.append("\n\n" + CodeBuilderHelper.tabulation(3))
			.append("logger.info(\"Fetching data from \" + this.service.getClass().getName() + \" service with \" + params.getPages() + \" page(s) and starting page \" + params.getStart());")
			.append("\n" + CodeBuilderHelper.tabulation(3))
			.append(this.clazz.getPackageName() + ".model.ICollectionModel<?> collectionModel = service.withParameters(params).httpGet();")
			.append("\n" + CodeBuilderHelper.tabulation(3))
			.append("super.modCount -= 1;")
			.append("\n" + CodeBuilderHelper.tabulation(3))
			.append("super.addAll((java.util.Collection<? extends E>) collectionModel.getElements());")
			.append("\n" + CodeBuilderHelper.tabulation(2))
			.append("}")
			.append("\n\n" + CodeBuilderHelper.tabulation(2))
			.append("return super.get(index);")
			.toString();
	}

	private void addSerialVersionNumberField() {
		FieldMeta field = new FieldMeta();
		
		field.setName("serialVersionUID");
		field.setVisibility(Visibility.PRIVATE);
		field.setFinalField(true);
		field.setStaticField(true);
		field.setType("long");
		field.setInitValue("7773182901940582171L");
		
		this.clazz.addField(field);
	}
	
	private void addLoggerField() {
		FieldMeta field = new FieldMeta();
		
		field.setName("logger");
		field.setVisibility(Visibility.PRIVATE);
		field.setFinalField(true);
		field.setStaticField(true);
		field.setType(Logger.class.getName());
		field.setInitValue("Logger.getLogger(EagerDataList.class.getName())");
		
		this.clazz.addField(field);
	}

	private void addVirtualSizeField() {
		FieldMeta field = new FieldMeta();
		
		field.setName("virtualSize");
		field.setVisibility(Visibility.PRIVATE);
		field.setType("int");
		
		this.clazz.addField(field);
	}

	private void addServiceField() {
		FieldMeta field = new FieldMeta();
		
		field.setName("service");
		field.setVisibility(Visibility.PRIVATE);
		field.setType(this.clazz.getPackageName() + ".service." + ServiceFetchInterfaceGenerator.CLASS_NAME);
		field.setGenericType("?");
		
		this.clazz.addField(field);
	}
	
	public ClassMeta getClazz() {
		return clazz;
	}
	
	public EagerDataListBuilder withClazz(ClassMeta clazz) {
		this.clazz = clazz;
		return this;
	}
}