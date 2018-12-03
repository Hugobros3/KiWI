# KiWI | Kotlin While Interpreter

This is a simple, functional-programming style interpreter for the While class of languages.
It also comes with a simplified DSL for you to write your programs in.

This interpreter was made as a student project for the Calculability & Complexity course @Unamur.

## Usage

TODO

## DSL example

```kotlin

v(1) assign hd(v(0))
v(2) assign tl(v(0))
v(3) assign nil

loop(v(1)) {
	v(3) assign cons(hd(v(1)), v(3))
	v(1) assign tl(v(1))
}

v(1) assign v(2)

loop(v(3)) {
	v(1) assign cons(hd(v(3)), v(1))
	v(3) assign tl(v(3))
}
			
```