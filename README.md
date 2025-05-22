# ICS4U Final Project – *TataTaiko*

This rhythm game is inspired by Taiko no Tatsujin, the Japanese arcade drumming game. It is a functional, incomplete clone/simulator developed in Java with Swing as a final project for ICS4U.  
(Current limitations: no drumrolls, partial feature set.)

## Dependencies

```bash
Java >= 20             # Earlier versions may still work, but are untested
Apache Commons Codec >= 1.18 # SHA hashing, security.
JLayer >= 1.0.1        # https://github.com/umjammer/jlayer
Python 3               # Used for the TJA to BIN conversion tool
```

## License

```text
See LICENSE.md for license details.
```

## Usage Instructions

To add songs to the game:

1. Go to the ESE TJA editor:  
   https://ese.tjadataba.se/ESE/ESE

2. Download your song as a `.tja` file.

3. Convert the associated `.ogg` file to `.mp3`.

4. Use the included **TJA-to-BIN** converter tool when launching the game.

5. Place both the `.mp3` and resulting `.bin` file in the `Songs/` directory.
