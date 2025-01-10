/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> ѱ 2025.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2025 Anton Astafiev <anton@astafiev.me>. All rights reserved.
 *
 * Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package ru.elliptica.collections;

import org.openjdk.jmh.annotations.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Антон А. Астафьев {@literal <anton@astafiev.me>} (Anton A. Astafiev)
 * @version 0.2 (2025)
 */
@State(Scope.Thread)
@Fork(value = 1, warmups = 2)
@Threads(Threads.MAX)
@Timeout(time = 15, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
public class TrieBench {
	StringTrie tr;
	List<String> words;
	Set<String> wordMapReady;
	Set<String> wordMapFresh;

	@Setup
	public void setUp() {
		tr = new StringTrie(Words.POSSIBLE_TRUE_VALS);
		words = List.of(Words.POSSIBLE_TRUE_VALS);
		wordMapReady = new HashSet<>(words);
		wordMapFresh = new HashSet<>();
		for (String word: words) {
			String fresh = String.copyValueOf(word.toCharArray());
			wordMapFresh.add(fresh);
		}
	}

	@Benchmark
	@GroupThreads(1)
	@Group("TrieSearch")
	public void benchTrieSearchCycle() {
		tr.containsAll(words);
	}

	@Benchmark
	@GroupThreads(1)
	@Group("HashReady")
	public void benchHashReadySearchCycle() {
		wordMapReady.containsAll(words);
	}

	@Benchmark
	@GroupThreads(1)
	@Group("HashFresh")
	public void benchHashFreshSearchCycle() {
		wordMapFresh.containsAll(words);
	}

}
