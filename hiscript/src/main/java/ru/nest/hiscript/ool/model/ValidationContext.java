package ru.nest.hiscript.ool.model;

import java.util.ArrayList;
import java.util.List;

public class ValidationContext {
	public List errors = new ArrayList();

	public int valueType;

	public HiClass type;

	public Node node;
}
