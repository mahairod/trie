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
import org.openjdk.jmh.infra.Blackhole;
import ru.elliptica.collections.Trie.TrieIndexVersion;
import ru.elliptica.collections.Trie.VocVersion;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Антон А. Астафьев {@literal <anton@astafiev.me>} (Anton A. Astafiev)
 * @version 0.2 (2025)
 */
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Threads(Threads.MAX)
@Timeout(time = 60, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(time = 1, iterations = 2)
@Measurement(time = 1, iterations = 7)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@CompilerControl(CompilerControl.Mode.INLINE)
public class TrieBench {
	List<String> words;
	Set<String> wordMapReady;
	Set<String> wordMapFresh;

	List<String> bigList;

	@State(Scope.Benchmark)
	public static class Plan {
		Trie tr;

		@Param({ "COMPUTED_FULL_IND", "COMPUTED_NOCOND_IND", "CACHED"})
		public VocVersion vocab = VocVersion.COMPUTED_NOCOND_IND;

		@Param({ "FLAT", "COMPRESSED"})
		public TrieIndexVersion index = TrieIndexVersion.FLAT;

		@Setup(Level.Trial)
		@Group("TrieSearch")
		public void setUp(TrieBench bench) {
			bench.words = List.of(Words.POSSIBLE_TRUE_VALS);
			switch (index) {
				case FLAT -> tr = new Trie(Words.POSSIBLE_TRUE_VALS, vocab);
				case COMPRESSED -> tr = new CompressedTrie(Words.POSSIBLE_TRUE_VALS, vocab);
			}
		}
	}

	@Setup(Level.Iteration)
	public void setUp(Blackhole blackhole) {
		words = List.of(Words.POSSIBLE_TRUE_VALS);
		wordMapReady = new HashSet<>(words);
		wordMapFresh = new HashSet<>();
		for (String word: words) {
			String fresh = String.copyValueOf(word.toCharArray());
			wordMapFresh.add(fresh);
		}
		bigList = Words.loadStrings();
	}

	@Benchmark
	@GroupThreads(3)
//	@Group("TrieSearch")
	@Warmup(time = 1, iterations = 5)
	@Measurement(time = 1, iterations = 15)
	public void benchTrieSearchCycle(Blackhole blackhole, Plan plan) {
		blackhole.consume(plan.tr.containsAllStrings(words));
	}

	@Benchmark
	@GroupThreads(3)
//	@Group("TrieSearch")
	@Warmup(time = 1, iterations = 5)
	@Measurement(time = 1, iterations = 15)
	public void benchTrieSearchVocab(Blackhole blackhole, Plan plan) {
		blackhole.consume(plan.tr.containsAllStrings(bigList));
	}

	@Benchmark
	@GroupThreads(2)
//	@Group("HashReady")
	public void benchHashReadySearchVocab(Blackhole blackhole) {
		blackhole.consume(wordMapReady.containsAll(bigList));
	}

	@Benchmark
	@GroupThreads(2)
//	@Group("HashReady")
	public void benchHashReadySearchCycle(Blackhole blackhole) {
		blackhole.consume(wordMapReady.containsAll(words));
	}

	@Benchmark
	@GroupThreads(2)
//	@Group("HashFresh")
	public void benchHashFreshSearchCycle(Blackhole blackhole) {
		blackhole.consume(wordMapFresh.containsAll(words));
	}

}
