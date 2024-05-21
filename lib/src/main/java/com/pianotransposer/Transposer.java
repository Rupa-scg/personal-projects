package com.pianotransposer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class Transposer {

	private static final int NOTES_IN_OCTAVE = 12;
	private static final int MIN_OCTAVE = -3;
	private static final int MAX_OCTAVE = 5;
	private static final int MIN_NOTE = 1;
	private static final int MAX_NOTE = 12;

	public static void main(final String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: java -jar PianoTransposer.jar <inputFile> <semitone> <outputFile>");
			System.exit(1);
		}

		final String inputFilePath = args[0];
		final int semitones = Integer.parseInt(args[1]);
		final String outputFilePath = args[2];

		try {
			final List<int[]> notes = readNotes(inputFilePath);
			final List<int[]> transposedNotes = transposeNotes(notes, semitones);
			validateNotes(transposedNotes);
			writeNotes(outputFilePath, transposedNotes);
		} catch (IOException | IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private static List<int[]> readNotes(final String inputFilePath) throws IOException {
		final Gson gson = new Gson();
		try (FileReader reader = new FileReader(inputFilePath)) {
			final Type noteListType = new TypeToken<List<int[]>>() {
			}.getType();
			return gson.fromJson(reader, noteListType);
		}
	}

	private static void writeNotes(final String outputFilePath, final List<int[]> notes) throws IOException {
		final Gson gson = new Gson();
		try (FileWriter writer = new FileWriter(outputFilePath)) {
			gson.toJson(notes, writer);
		}
	}

	private static List<int[]> transposeNotes(final List<int[]> notes, final int semitones) {
		for (final int[] note : notes) {
			final int totalSemitones = note[0] * NOTES_IN_OCTAVE + note[1] + semitones;
			note[0] = totalSemitones / NOTES_IN_OCTAVE;
			note[1] = totalSemitones % NOTES_IN_OCTAVE;
			if (note[1] <= 0) {
				note[0] -= 1;
				note[1] += NOTES_IN_OCTAVE;
			}
		}
		return notes;
	}

	private static void validateNotes(final List<int[]> notes) {
		for (final int[] note : notes) {
			if (note[0] < MIN_OCTAVE || note[0] > MAX_OCTAVE || note[1] < MIN_NOTE || note[1] > MAX_NOTE
					|| (note[0] == MIN_OCTAVE && note[1] < 10) || (note[0] == MAX_OCTAVE && note[1] > 1)) {
				throw new IllegalArgumentException("Note out of range: " + note[0] + ", " + note[1]);
			}
		}
	}
}
