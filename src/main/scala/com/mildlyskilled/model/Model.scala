package com.mildlyskilled.model


sealed trait ChuckNorrisEntry

final case class Joke(id: Int, joke: String, categories: List[String]) extends ChuckNorrisEntry {
  override def toString: String = joke
}

final case class JokeEntry(`type`: String, value: Joke) extends ChuckNorrisEntry {
  override def toString: String = value.joke
}

final case class JokeEntries(`type`: String, value: List[Joke]) extends ChuckNorrisEntry {
  override def toString: String = value.map(joke => joke.joke).mkString("\n")
}

