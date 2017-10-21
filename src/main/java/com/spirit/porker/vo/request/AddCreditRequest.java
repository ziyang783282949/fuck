package com.spirit.porker.vo.request;

public class AddCreditRequest extends BaseRequest{
	private int id;
	private int weight;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

}
