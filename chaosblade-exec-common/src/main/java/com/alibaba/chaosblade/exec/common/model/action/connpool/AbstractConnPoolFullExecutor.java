/*
 * Copyright 2025 The ChaosBlade Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.chaosblade.exec.common.model.action.connpool;

import com.alibaba.chaosblade.exec.common.aop.EnhancerModel;
import com.alibaba.chaosblade.exec.common.util.ThreadUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Changjun Xiao */
public abstract class AbstractConnPoolFullExecutor implements ConnectionPoolFullExecutor {

  public static final int DEFAULT_MAX_POOL_SIZE = 100;
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConnPoolFullExecutor.class);

  private CopyOnWriteArraySet<Connection> connectionHolder = new CopyOnWriteArraySet<Connection>();

  private Object lock = new Object();
  private volatile boolean isRunning;
  private volatile ScheduledExecutorService executorService;

  @Override
  public void run(EnhancerModel enhancerModel) throws Exception {}

  @Override
  public void full(final DataSource dataSource) {
    if (dataSource == null) {
      LOGGER.warn("数据源为空");
      return;
    }
    if (connectionHolder.size() > 0) {
      LOGGER.info("连接池满载已启动");
      return;
    }
    isRunning = true;

    int maxPoolSize = getMaxPoolSize();
    final int poolSize = maxPoolSize <= 0 ? DEFAULT_MAX_POOL_SIZE : maxPoolSize;
    LOGGER.info("开始执行连接池满载, poolSize: {}", poolSize);

    synchronized (lock) {
      if (executorService == null
          || executorService.isShutdown()
          || executorService.isTerminated()) {
        executorService = ThreadUtil.createScheduledExecutorService();
      }
    }
    executorService.scheduleWithFixedDelay(
        new Runnable() {
          @Override
          public void run() {
            try {
              for (int i = 0; i < poolSize; i++) {
                Connection connection = dataSource.getConnection();
                if (connection != null) {
                  connectionHolder.add(connection);
                }
              }
              LOGGER.info("执行连接池满载成功");
            } catch (SQLException e) {
              LOGGER.info("连接池满载, {}", e.getMessage());
            } catch (Exception e) {
              LOGGER.warn("获取数据库连接异常", e);
            }
          }
        },
        0,
        10,
        TimeUnit.SECONDS);
  }

  @Override
  public void revoke() {
    if (connectionHolder.size() == 0) {
      return;
    }
    executorService.shutdownNow();
    for (Connection connection : connectionHolder) {
      try {
        connection.close();
      } catch (Exception e) {
        LOGGER.warn("关闭数据库连接异常", e);
      }
    }
    connectionHolder.clear();
    isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }
}
