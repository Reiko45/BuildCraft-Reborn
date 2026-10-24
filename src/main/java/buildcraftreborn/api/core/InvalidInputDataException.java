/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.core;

import java.io.IOException;

/**
 * Indicates that we failed to load from NBT or some other file.
 */
public class InvalidInputDataException extends IOException {
    public InvalidInputDataException() {
    }

    public InvalidInputDataException(String message) {
        super(message);
    }

    public InvalidInputDataException(Throwable cause) {
        super(cause);
    }

    public InvalidInputDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
