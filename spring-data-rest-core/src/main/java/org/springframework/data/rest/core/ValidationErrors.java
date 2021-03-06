/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.rest.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.util.Assert;
import org.springframework.validation.AbstractPropertyBindingResult;
import org.springframework.validation.Errors;

/**
 * An {@link Errors} implementation for use in the events mechanism of Spring Data REST. Customizes actual field lookup
 * by using a {@link PersistentPropertyAccessor} for actual value lookups.
 * 
 * @author Jon Brisbin
 * @author Oliver Gierke
 */
public class ValidationErrors extends AbstractPropertyBindingResult {

	private static final long serialVersionUID = 8141826537389141361L;

	private final PersistentPropertyAccessor accessor;
	private PersistentEntity<?, ?> entity;

	/**
	 * Creates a new {@link ValidationErrors} instance for the given source object and {@link PersistentEntity}.
	 * 
	 * @param source the source object to gather validation errors on, must not be {@literal null}.
	 * @param entity the {@link PersistentEntity} for the given source instance, must not be {@literal null}.
	 */
	public ValidationErrors(Object source, PersistentEntity<?, ?> entity) {

		super(source.getClass().getSimpleName());

		Assert.notNull(source, "Entity must not be null!");
		Assert.notNull(entity, "PersistentEntity must not be null!");
		Assert.isTrue(entity.getType().isInstance(source),
				"Given source object is not of type of the given PersistentEntity");

		this.entity = entity;
		this.accessor = entity.getPropertyAccessor(source);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.validation.AbstractPropertyBindingResult#getPropertyAccessor()
	 */
	@Override
	public ConfigurablePropertyAccessor getPropertyAccessor() {

		return new DirectFieldAccessor(getTarget()) {

			@Override
			public Object getPropertyValue(String propertyName) throws BeansException {
				PersistentProperty<?> property = entity.getPersistentProperty(propertyName);
				return property == null ? null : accessor.getProperty(property);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.validation.AbstractBindingResult#getFieldValue(java.lang.String)
	 */
	@Override
	public Object getFieldValue(String field) {

		if (field.contains(".")) {
			return super.getFieldValue(field);
		}

		return accessor.getProperty(entity.getPersistentProperty(field));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.validation.AbstractBindingResult#getTarget()
	 */
	@Override
	public Object getTarget() {
		return accessor.getBean();
	}
}
