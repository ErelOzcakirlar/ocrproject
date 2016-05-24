package edu.iu.ocrproject;

public class Character {
	
	private int left_top;
	private int left_bottom;
	private int right_top;
	private int right_bottom;
	
	private char characterValue;

	public int getLeft_top() {
		return left_top;
	}

	public void setLeft_top(int left_top) {
		this.left_top = left_top;
	}

	public int getLeft_bottom() {
		return left_bottom;
	}

	public void setLeft_bottom(int left_bottom) {
		this.left_bottom = left_bottom;
	}

	public int getRight_top() {
		return right_top;
	}

	public void setRight_top(int right_top) {
		this.right_top = right_top;
	}

	public int getRight_bottom() {
		return right_bottom;
	}

	public void setRight_bottom(int right_bottom) {
		this.right_bottom = right_bottom;
	}

	public char getCharacterValue() {
		return characterValue;
	}

	public void setCharacterValue(char characterValue) {
		this.characterValue = characterValue;
	}
	

}
