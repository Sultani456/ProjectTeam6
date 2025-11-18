package com.project.team6.model.boardUtilities.generators;

/** Modes:
 *  NONE: perimeter walls only, empty interior
 *  PROVIDED: perimeter walls + programmer-provided internal barriers; start/exit randomized on west/east edges
 *  RANDOM: perimeter walls + random internal barriers
 *  TEXT: read terrain from a text file on the classpath (e.g., "maps/level1.txt")*/
public enum BarrierMode { NONE, PROVIDED, TEXT, RANDOM }
