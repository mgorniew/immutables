/*
 * Copyright 2019 Immutables Authors and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.immutables.criteria.matcher;


import org.immutables.criteria.expression.Call;
import org.immutables.criteria.expression.Expression;
import org.immutables.criteria.expression.Expressions;
import org.immutables.criteria.expression.Operators;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Criteria for comparables (like {@code >, <=, >} and ranges).
 *
 * @param <R> root criteria type
 */
public interface ComparableMatcher<R, V extends Comparable<? super V>> extends ObjectMatcher<R, V>, OrderMatcher {

  /**
   * Checks that attribute is less than (but not equal to) {@code upper}.
   * <p>Use {@link #isAtMost(Comparable)} for less <i>or equal</i> comparison</p>
   */
  default R isLessThan(V upper) {
    Objects.requireNonNull(upper, "upper");
    return Matchers.extract(this).<R, Object>factory().createRoot(e -> Expressions.call(Operators.LESS_THAN, e, Expressions.constant(upper)));
  }

  /**
   * Checks that attribute is greater than (but not equal to)  {@code lower}.
   * <p>Use {@link #isAtLeast(Comparable)} for greater <i>or equal</i> comparison</p>
   */
  default R isGreaterThan(V lower) {
    Objects.requireNonNull(lower, "lower");
    return Matchers.extract(this).<R, Object>factory().createRoot(e -> Expressions.call(Operators.GREATER_THAN, e, Expressions.constant(lower)));
  }

  /**
   * Checks that attribute is less than or equal to {@code upperInclusive}.
   */
  default R isAtMost(V upperInclusive) {
    Objects.requireNonNull(upperInclusive, "upperInclusive");
    return Matchers.extract(this).<R, Object>factory().createRoot(e -> Expressions.call(Operators.LESS_THAN_OR_EQUAL, e, Expressions.constant(upperInclusive)));
  }

  /**
   * Checks that attribute is greater or equal to {@code lowerInclusive}.
   */
  default R isAtLeast(V lowerInclusive) {
    Objects.requireNonNull(lowerInclusive, "lowerInclusive");
    return Matchers.extract(this).<R, Object>factory().createRoot(e -> Expressions.call(Operators.GREATER_THAN_OR_EQUAL, e, Expressions.constant(lowerInclusive)));
  }

  /**
   * Checks that attribute is in {@code [lower, upper]} range (lower included, upper included).
   * @param lowerInclusive lower value (inclusive)
   * @param upperInclusive upper value (inclusive)
   */
  default R isBetween(V lowerInclusive, V upperInclusive) {
    Objects.requireNonNull(lowerInclusive, "lowerInclusive");
    Objects.requireNonNull(upperInclusive, "upperInclusive");
    final UnaryOperator<Expression> unary = expr -> {
      final Call lower = Expressions.call(Operators.GREATER_THAN_OR_EQUAL, expr, Expressions.constant(lowerInclusive));
      final Call upper = Expressions.call(Operators.LESS_THAN_OR_EQUAL, expr, Expressions.constant(upperInclusive));
      return Expressions.and(lower, upper);
    };

    return Matchers.extract(this).<R, Object>factory().createRoot(unary);
  }

  /**
   * Self-type for this matcher
   */
  interface Self<V extends Comparable<? super V>> extends Template<Self<V>, V>, Disjunction<Template<Self<V>, V>> {}

  interface Template<R, V extends Comparable<? super V>> extends ComparableMatcher<R, V>, WithMatcher<R, Self<V>>, NotMatcher<R, Self<V>> {}

  @SuppressWarnings("unchecked")
  static <R> CriteriaCreator<R> creator() {
    class Local extends ContextHolder.AbstractHolder implements Self {
      private Local(CriteriaContext context) {
        super(context);
      }
    }

    return ctx -> (R) new Local(ctx);
  }

}
