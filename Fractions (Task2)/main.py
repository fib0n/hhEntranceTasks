#!/usr/local/bin/python3
import sys
from fractions import gcd


# http://stackoverflow.com/a/2267428
def baseN(num, b, numerals="0123456789abcdefghijklmnopqrstuvwxyz"):
    return ((num == 0) and numerals[0]) or (baseN(num // b, b, numerals).lstrip(numerals[0]) + numerals[num % b])


def InputIterator(f):
  for line in f:
    for value in line.split():
      yield value

inp = InputIterator(open('input.txt', 'r'))
out = open('output.txt', 'w')

while True:
  try:
    a, b, k = int(next(inp)), int(next(inp)), int(next(inp))
  except StopIteration:
    break

  gcdAB = gcd(a, b)
  a //= gcdAB
  b //= gcdAB
  out.write(str(baseN(a // b, k)))
  if b == 1:
    out.write('\n')
    continue

  a %= b
  out.write('.')
  while gcd(b, k) > 1:
    a *= k
    out.write(str(baseN(a // b, k)))
    gcdAB = gcd(a, b)
    a //= gcdAB
    b //= gcdAB
    a %= b

  if b == 1:
    out.write('\n')
    continue

  rest = a
  out.write('(')

  while True:
    a *= k
    out.write(str(baseN(a // b, k)))
    a %= b
    if rest == a:
      break
  out.write(')\n')
