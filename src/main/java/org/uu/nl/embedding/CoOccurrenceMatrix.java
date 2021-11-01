package org.uu.nl.embedding;

/**
 * @author Jurian Baas
 */
public interface CoOccurrenceMatrix {

	int nrOfContextVectors();
	int nrOfFocusVectors();
	double max();
	int cIdx_I(int i);
	int cIdx_J(int j);
	float cIdx_C(int i);
	int coOccurrenceCount();
	void shuffle();
}
