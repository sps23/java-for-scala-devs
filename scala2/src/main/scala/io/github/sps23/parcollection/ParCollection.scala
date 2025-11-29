package io.github.sps23.parcollection

import java.util.concurrent.{ExecutorService, Executors}
import scala.collection.parallel.CollectionConverters.ImmutableSeqIsParallelizable
import scala.collection.parallel.ExecutionContextTaskSupport

/** Demonstrates how Scala's parallel collections compare to plain sequential processing. */
object ParCollection {

  /** Applies the provided function sequentially using `List#map`. */
  def applyF[In, Out](input: Seq[In], f: In => Out): Seq[Out] =
    input.map(f)

  def aggregateF[In, Out](
      input: Seq[In],
      f: In                 => Out,
      aggregate: (Out, Out) => Out,
      zero: Out
  ): Out =
    input.foldLeft(zero)((acc, in) => aggregate(acc, f(in)))

  /** Applies the provided function using Scala's default parallel collections backing pool. */
  def applyParF[In, Out](input: Seq[In], f: In => Out): Seq[Out] =
    input.par.map(f).seq

  def aggregateParF[In, Out](
      input: Seq[In],
      f: In                 => Out,
      aggregate: (Out, Out) => Out,
      zero: Out
  ): Out =
    input.par.aggregate(zero)((acc, in) => aggregate(acc, f(in)), aggregate)

  /** Applies the provided function using a manually sized fixed thread pool. */
  def applyParFixedF[In, Out](input: Seq[In], f: In => Out, fixedNumberOfThreads: Int): Seq[Out] = {
    require(fixedNumberOfThreads > 0, "fixedNumberOfThreads must be positive")
    val fixedThreadPool = Executors.newFixedThreadPool(fixedNumberOfThreads)
    try
      applyParF(input, f, fixedThreadPool)
    finally fixedThreadPool.shutdown()
  }

  def aggregateParFixedF[In, Out](
      input: Seq[In],
      f: In                 => Out,
      aggregate: (Out, Out) => Out,
      zero: Out,
      fixedNumberOfThreads: Int
  ): Out = {
    require(fixedNumberOfThreads > 0, "fixedNumberOfThreads must be positive")
    val fixedThreadPool = Executors.newFixedThreadPool(fixedNumberOfThreads)
    try
      aggregateParF(input, f, aggregate, zero, fixedThreadPool)
    finally fixedThreadPool.shutdown()
  }

  /** Applies the provided function using a manually sized work-stealing pool. */
  def applyParForkF[In, Out](input: Seq[In], f: In => Out, parallelism: Int): Seq[Out] = {
    require(parallelism > 0, "parallelism must be positive")
    val forkJoinThreadPool = Executors.newWorkStealingPool(parallelism)
    try
      applyParF(input, f, forkJoinThreadPool)
    finally forkJoinThreadPool.shutdown()
  }

  def aggregateParForkF[In, Out](
      input: Seq[In],
      f: In                 => Out,
      aggregate: (Out, Out) => Out,
      zero: Out,
      parallelism: Int
  ): Out = {
    require(parallelism > 0, "parallelism must be positive")
    val forkJoinThreadPool = Executors.newWorkStealingPool(parallelism)
    try
      aggregateParF(input, f, aggregate, zero, forkJoinThreadPool)
    finally forkJoinThreadPool.shutdown()
  }

  private def applyParF[In, Out](
      input: Seq[In],
      f: In => Out,
      executor: ExecutorService
  ): Seq[Out] = {
    val ec            = scala.concurrent.ExecutionContext.fromExecutor(executor)
    val parCollection = input.par
    parCollection.tasksupport = new ExecutionContextTaskSupport(ec)
    parCollection.map(f).seq
  }

  private def aggregateParF[In, Out](
      input: Seq[In],
      f: In                 => Out,
      aggregate: (Out, Out) => Out,
      zero: Out,
      executor: ExecutorService
  ): Out = {
    val ec            = scala.concurrent.ExecutionContext.fromExecutor(executor)
    val parCollection = input.par
    parCollection.tasksupport = new ExecutionContextTaskSupport(ec)
    parCollection.aggregate(zero)((acc, in) => aggregate(acc, f(in)), aggregate)
  }
}
