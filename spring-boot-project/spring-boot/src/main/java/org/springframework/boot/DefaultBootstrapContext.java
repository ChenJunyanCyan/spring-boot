/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.util.Assert;

/**
 * 默认的ConfigurableBootstrapContext实现类
 * 在Spring Boot应用启动的早期阶段提供临时的实例注册和共享机制
 * 用于在ApplicationContext创建之前管理和传递必要的组件
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public class DefaultBootstrapContext implements ConfigurableBootstrapContext {

	/**
	 * 实例供应器映射，存储类型到InstanceSupplier的映射关系
	 * InstanceSupplier用于延迟创建实例
	 */
	private final Map<Class<?>, InstanceSupplier<?>> instanceSuppliers = new HashMap<>();

	/**
	 * 已创建的实例缓存，用于单例模式的实例复用
	 * 键为类型，值为实际创建的实例对象
	 */
	private final Map<Class<?>, Object> instances = new HashMap<>();

	/**
	 * 事件广播器，用于发布引导上下文相关的事件
	 * 主要用于在引导上下文关闭时通知监听器
	 */
	private final ApplicationEventMulticaster events = new SimpleApplicationEventMulticaster();

	/**
	 * 注册指定类型的实例供应器
	 * 如果该类型已经注册过，则会替换原有的供应器
	 * 
	 * @param type 要注册的实例类型
	 * @param instanceSupplier 实例供应器，用于延迟创建实例
	 * @param <T> 实例类型
	 */
	@Override
	public <T> void register(Class<T> type, InstanceSupplier<T> instanceSupplier) {
		register(type, instanceSupplier, true);
	}

	/**
	 * 如果指定类型尚未注册，则注册实例供应器
	 * 如果该类型已经注册过，则不会进行任何操作（保留原有注册）
	 * 
	 * @param type 要注册的实例类型
	 * @param instanceSupplier 实例供应器，用于延迟创建实例
	 * @param <T> 实例类型
	 */
	@Override
	public <T> void registerIfAbsent(Class<T> type, InstanceSupplier<T> instanceSupplier) {
		register(type, instanceSupplier, false);
	}

	/**
	 * 内部注册方法，执行实际的注册逻辑
	 * 
	 * @param type 要注册的实例类型
	 * @param instanceSupplier 实例供应器
	 * @param replaceExisting 是否替换已存在的注册
	 * @param <T> 实例类型
	 */
	private <T> void register(Class<T> type, InstanceSupplier<T> instanceSupplier, boolean replaceExisting) {
		// 验证参数不能为null
		Assert.notNull(type, "'type' must not be null");
		Assert.notNull(instanceSupplier, "'instanceSupplier' must not be null");
		// 使用同步保证线程安全
		synchronized (this.instanceSuppliers) {
			// 检查是否已经注册过该类型
			boolean alreadyRegistered = this.instanceSuppliers.containsKey(type);
			// 如果允许替换或者尚未注册，则执行注册
			if (replaceExisting || !alreadyRegistered) {
				// 如果实例已经被创建，则不允许再注册供应器
				Assert.state(!this.instances.containsKey(type), () -> type.getName() + " has already been created");
				// 将类型和供应器映射保存
				this.instanceSuppliers.put(type, instanceSupplier);
			}
		}
	}

	/**
	 * 检查指定类型是否已经注册
	 * 
	 * @param type 要检查的实例类型
	 * @param <T> 实例类型
	 * @return 如果该类型已注册返回true，否则返回false
	 */
	@Override
	public <T> boolean isRegistered(Class<T> type) {
		synchronized (this.instanceSuppliers) {
			return this.instanceSuppliers.containsKey(type);
		}
	}

	/**
	 * 获取指定类型已注册的实例供应器
	 * 
	 * @param type 要查询的实例类型
	 * @param <T> 实例类型
	 * @return 已注册的实例供应器，如果未注册则返回null
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> InstanceSupplier<T> getRegisteredInstanceSupplier(Class<T> type) {
		synchronized (this.instanceSuppliers) {
			return (InstanceSupplier<T>) this.instanceSuppliers.get(type);
		}
	}

	/**
	 * 添加引导上下文关闭事件监听器
	 * 当引导上下文关闭时，会发布BootstrapContextClosedEvent事件通知所有监听器
	 * 
	 * @param listener 要添加的事件监听器
	 */
	@Override
	public void addCloseListener(ApplicationListener<BootstrapContextClosedEvent> listener) {
		this.events.addApplicationListener(listener);
	}

	/**
	 * 获取指定类型的实例
	 * 如果该类型未注册，则抛出IllegalStateException异常
	 * 
	 * @param type 要获取的实例类型
	 * @param <T> 实例类型
	 * @return 实例对象
	 * @throws IllegalStateException 如果该类型未注册
	 */
	@Override
	public <T> T get(Class<T> type) throws IllegalStateException {
		return getOrElseThrow(type, () -> new IllegalStateException(type.getName() + " has not been registered"));
	}

	/**
	 * 获取指定类型的实例，如果未注册则返回提供的默认值
	 * 
	 * @param type 要获取的实例类型
	 * @param other 默认值，当类型未注册时返回此值
	 * @param <T> 实例类型
	 * @return 实例对象或默认值
	 */
	@Override
	public <T> T getOrElse(Class<T> type, T other) {
		return getOrElseSupply(type, () -> other);
	}

	/**
	 * 获取指定类型的实例，如果未注册则通过Supplier提供默认值
	 * 如果实例已注册且为单例模式，会复用已创建的实例
	 * 
	 * @param type 要获取的实例类型
	 * @param other 默认值供应器，当类型未注册时调用此供应器获取默认值
	 * @param <T> 实例类型
	 * @return 实例对象或默认值
	 */
	@Override
	public <T> T getOrElseSupply(Class<T> type, Supplier<T> other) {
		synchronized (this.instanceSuppliers) {
			// 查找是否已注册该类型的供应器
			InstanceSupplier<?> instanceSupplier = this.instanceSuppliers.get(type);
			// 如果已注册则获取实例，否则使用提供的默认值供应器
			return (instanceSupplier != null) ? getInstance(type, instanceSupplier) : other.get();
		}
	}

	/**
	 * 获取指定类型的实例，如果未注册则抛出Supplier提供的异常
	 * 
	 * @param type 要获取的实例类型
	 * @param exceptionSupplier 异常供应器，当类型未注册时调用此供应器生成异常
	 * @param <T> 实例类型
	 * @param <X> 异常类型
	 * @return 实例对象
	 * @throws X 如果该类型未注册，则抛出此异常
	 */
	@Override
	public <T, X extends Throwable> T getOrElseThrow(Class<T> type, Supplier<? extends X> exceptionSupplier) throws X {
		synchronized (this.instanceSuppliers) {
			// 查找是否已注册该类型的供应器
			InstanceSupplier<?> instanceSupplier = this.instanceSuppliers.get(type);
			// 如果未注册，则抛出异常
			if (instanceSupplier == null) {
				throw exceptionSupplier.get();
			}
			// 获取并返回实例
			return getInstance(type, instanceSupplier);
		}
	}

	/**
	 * 获取实例的核心方法，负责通过InstanceSupplier创建实例
	 * 对于单例模式的实例，会缓存以便后续复用
	 * 
	 * @param type 实例类型
	 * @param instanceSupplier 实例供应器
	 * @param <T> 实例类型
	 * @return 创建或缓存的实例对象
	 */
	@SuppressWarnings("unchecked")
	private <T> T getInstance(Class<T> type, InstanceSupplier<?> instanceSupplier) {
		// 先从缓存中查找是否已创建过实例
		T instance = (T) this.instances.get(type);
		// 如果缓存中没有，则通过供应器创建新实例
		if (instance == null) {
			instance = (T) instanceSupplier.get(this);
			// 如果是单例模式，则将实例缓存起来
			if (instanceSupplier.getScope() == Scope.SINGLETON) {
				this.instances.put(type, instance);
			}
		}
		return instance;
	}

	/**
	 * 关闭引导上下文，当ApplicationContext准备完成时调用此方法
	 * 会发布BootstrapContextClosedEvent事件，通知所有注册的监听器
	 * 监听器可以在此时机将引导上下文中的实例转移到ApplicationContext中
	 * 
	 * @param applicationContext 已准备好的应用上下文
	 */
	public void close(ConfigurableApplicationContext applicationContext) {
		// 广播引导上下文关闭事件
		this.events.multicastEvent(new BootstrapContextClosedEvent(this, applicationContext));
	}

}
