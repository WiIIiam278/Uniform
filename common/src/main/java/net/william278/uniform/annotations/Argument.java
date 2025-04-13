/*
 * This file is part of Uniform, licensed under the GNU General Public License v3.0.
 *
 *  Copyright (c) Tofaa2
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.william278.uniform.annotations;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.element.ArgumentElement;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {

    String name() default "";
    Class<? extends ArgumentProvider<?>> parser();
    String[] parserProperties() default {};

    @NoArgsConstructor
    abstract class ArgumentProvider<S> {

        public abstract ArgumentElement<?, S> provide(@NotNull String name);

    }

    class StringArg extends ArgumentProvider<String> {
        @Override
        public ArgumentElement<?, String> provide(@NotNull String name) {
            return BaseCommand.string(name);
        }
    }

    class WordArg extends ArgumentProvider<String> {
        @Override
        public ArgumentElement<?, String> provide(@NotNull String name) {
            return BaseCommand.word(name);
        }
    }

    class BooleanArg extends ArgumentProvider<Boolean> {
        @Override
        public ArgumentElement<?, Boolean> provide(@NotNull String name) {
            return BaseCommand.bool(name);
        }
    }

    class GreedyStringArg extends ArgumentProvider<String> {
        @Override
        public ArgumentElement<?, String> provide(@NotNull String name) {
            return BaseCommand.greedyString(name);
        }
    }

    class IntegerArg extends ArgumentProvider<Integer> {
        @Override
        public ArgumentElement<?, Integer> provide(@NotNull String name) {
            return BaseCommand.intNum(name);
        }
    }

    @AllArgsConstructor
    class BoundedIntegerArg extends ArgumentProvider<Integer> {
        private final int min;
        private final Integer max;

        public BoundedIntegerArg(@NotNull String[] properties) {
            if (properties.length == 0) {
                throw new IllegalArgumentException("BoundedIntegerArg requires at least one property (min, max)");
            }
            this.min = Integer.parseInt(properties[0]);
            if (properties.length == 1) {
                this.max = null;
                return;
            }
            this.max = Integer.parseInt(properties[1]);
        }

        @Override
        public ArgumentElement<?, Integer> provide(@NotNull String name) {
            if (max == null) {
                return BaseCommand.intNum(name, min);
            }
            return BaseCommand.intNum(name, min, max);
        }
    }

    class FloatArg extends ArgumentProvider<Float> {
        @Override
        public ArgumentElement<?, Float> provide(@NotNull String name) {
            return BaseCommand.floatNum(name);
        }
    }

    @AllArgsConstructor
    class BoundedFloatArg extends ArgumentProvider<Float> {
        private final float min;
        private final Float max;

        public BoundedFloatArg(@NotNull String[] properties) {
            if (properties.length == 0) {
                throw new IllegalArgumentException("BoundedFloatArg requires at least one property (min, max)");
            }
            this.min = Float.parseFloat(properties[0]);
            if (properties.length == 1) {
                this.max = null;
                return;
            }
            this.max = Float.parseFloat(properties[1]);
        }

        @Override
        public ArgumentElement<?, Float> provide(@NotNull String name) {
            if (max == null) {
                return BaseCommand.floatNum(name, min);
            }
            return BaseCommand.floatNum(name, min, max);
        }
    }

    class DoubleArg extends ArgumentProvider<Double> {
        @Override
        public ArgumentElement<?, Double> provide(@NotNull String name) {
            return BaseCommand.doubleNum(name);
        }
    }

    @AllArgsConstructor
    class BoundedDoubleArg extends ArgumentProvider<Double> {
        private final double min;
        private final Double max;

        public BoundedDoubleArg(@NotNull String[] properties) {
            if (properties.length == 0) {
                throw new IllegalArgumentException("BoundedDoubleArg requires at least one property (min, max)");
            }
            this.min = Double.parseDouble(properties[0]);
            if (properties.length == 1) {
                this.max = null;
                return;
            }
            this.max = Double.parseDouble(properties[1]);
        }

        @Override
        public ArgumentElement<?, Double> provide(@NotNull String name) {
            if (max == null) {
                return BaseCommand.doubleNum(name, min);
            }
            return BaseCommand.doubleNum(name, min, max);
        }
    }

}
