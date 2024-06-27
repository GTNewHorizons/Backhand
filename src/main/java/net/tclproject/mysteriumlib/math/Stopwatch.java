package net.tclproject.mysteriumlib.math;

/** Class for measuring time passed. */
public class Stopwatch {

    /** The name of the stopwatch. */
    final String name;
    /** The time at which the stopwatch started counting. */
    long timeStart;
    /** How much times the stopwatch has been started and stopped. */
    double calls;
    /** The total time across all the time the stopwatch has been counting time. */
    double timeTotal;
    /** The maximum time ever taken between start and stop calls. */
    double timeMax;
    /** The minimum time ever taken between start and stop calls. */
    double timeMin;
    /** The time taken between the most recent start and stop calls. */
    double timeTaken;

    public Stopwatch(String stopwatchName) {
        this.name = stopwatchName;
        this.timeMax = -1.0D;
        this.timeMin = -1.0D;
    }

    /** Starts the counting. */
    public void start() {
        this.timeStart = System.nanoTime();
    }

    /** Stops the counting and updates the variables. */
    public void stop() {
        ++this.calls;
        double timeTaken = (System.nanoTime() - this.timeStart) / 1000000.0D;
        this.timeTotal += timeTaken;
        this.timeMax = this.timeMax == -1.0D ? timeTaken : Math.max(timeTaken, this.timeMax);
        this.timeMin = this.timeMin == -1.0D ? timeTaken : Math.min(timeTaken, this.timeMin);
    }

    /** Prints out all the variables in an easy to see manner. */
    @Override
    public String toString() {
        return String.format(
            "[%s]: Time [avg]: %3.2f ms, [min]: %3.2f ms, [max]: %3.2f ms",
            this.name,
            this.timeTotal / this.calls,
            this.timeMin,
            this.timeMax);
    }
}
