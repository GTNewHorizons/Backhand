package xonin.backhand.compat;

import jdk.jfr.Experimental;

/**
 * Marker interface for custom renderers that want to override the default offhand rendering
 * <br>
 * Note: This is added mostly for expediency. It probably isn't required when rendering code is written especially for
 * Backhand.
 */
@Experimental
public interface IOffhandRenderOptOut {
}
