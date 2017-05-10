/* 示例 */
CREATE TABLE t_example (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL
  COMMENT '名称',
  enabled CHAR(1) NOT NULL
  COMMENT '启用状态',
  create_time DATETIME NOT NULL DEFAULT now(),
  modify_time DATETIME NOT NULL DEFAULT now()
);

