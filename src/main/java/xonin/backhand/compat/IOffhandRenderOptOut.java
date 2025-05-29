package xonin.backhand.compat;

import org.jetbrains.annotations.ApiStatus;

/**
 * Marker interface for custom renderers that want to override the default offhand rendering
 * <br>
 * Note: This is added mostly for expediency. It probably isn't required when rendering code is written especially for
 * Backhand.
 */
@ApiStatus.Experimental
public interface IOffhandRenderOptOut {
}
