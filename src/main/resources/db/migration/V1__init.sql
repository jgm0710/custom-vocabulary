-- -----------------------------------------------------
-- Table `customvoca`.`member`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`member` (
    `member_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `bbs_count` INT(11) NOT NULL,
    `date_of_birth` DATE NULL DEFAULT NULL,
    `email` VARCHAR(255) NOT NULL,
    `gender` VARCHAR(255) NOT NULL,
    `join_id` VARCHAR(255) NOT NULL,
    `refresh_token` VARCHAR(255) NULL,
    `refresh_token_expiration_period_date_time` DATETIME(6) NULL DEFAULT NULL,
    `name` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NULL,
    `register_date` DATETIME(6) NULL DEFAULT NULL,
    `shared_vocabulary_count` INT(11) NOT NULL,
    `simple_address` VARCHAR(255) NULL DEFAULT NULL,
    `update_date` DATETIME(6) NULL DEFAULT NULL,
    PRIMARY KEY (`member_id`),
    UNIQUE INDEX `ukx-member-join-id` (`join_id` ASC),
    UNIQUE INDEX `ukx-member-nickname` (`nickname` ASC) )
    ENGINE = InnoDB
    AUTO_INCREMENT = 3
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`bbs`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`bbs` (
    `bbs_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `content` LONGTEXT NOT NULL,
    `like_count` INT(11) NOT NULL,
    `register_date` DATETIME(6) NULL DEFAULT NULL,
    `reply_count` INT(11) NOT NULL,
    `status` VARCHAR(255) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `update_date` DATETIME(6) NULL DEFAULT NULL,
    `views` INT(11) NOT NULL,
    `member_id` BIGINT(20) NOT NULL,
    PRIMARY KEY (`bbs_id`),
    INDEX `fkx-bbs-member` (`member_id` ASC),
    CONSTRAINT `fk-bbs-member`
    FOREIGN KEY (`member_id`)
    REFERENCES `customvoca`.`member` (`member_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`bbs_like`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`bbs_like` (
    `bbs_like_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `register_date` DATETIME(6) NULL DEFAULT NULL,
    `bbs_id` BIGINT(20) NULL DEFAULT NULL,
    `member_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`bbs_like_id`),
    INDEX `fkx-bbs_like-bbs` (`bbs_id` ASC),
    INDEX `fkx-bbs_like-member` (`member_id` ASC),
    CONSTRAINT `fk-bbs_like-bbs`
    FOREIGN KEY (`bbs_id`)
    REFERENCES `customvoca`.`bbs` (`bbs_id`),
    CONSTRAINT `fk-bbs_like-member`
    FOREIGN KEY (`member_id`)
    REFERENCES `customvoca`.`member` (`member_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`bbs_upload_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`bbs_upload_file` (
    `bbs_upload_file_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `file_download_uri` VARCHAR(1000) NULL DEFAULT NULL,
    `file_name` VARCHAR(255) NULL DEFAULT NULL,
    `file_stored_path` VARCHAR(255) NULL DEFAULT NULL,
    `file_type` VARCHAR(255) NULL DEFAULT NULL,
    `size` BIGINT(20) NULL DEFAULT NULL,
    `bbs_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`bbs_upload_file_id`),
    INDEX `fkx-bbs_upload_file-bbs` (`bbs_id` ASC),
    CONSTRAINT `fk-bbs_upload_file-bbs`
    FOREIGN KEY (`bbs_id`)
    REFERENCES `customvoca`.`bbs` (`bbs_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`category`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`category` (
    `category_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `division` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `orders` INT(11) NOT NULL,
    `vocabulary_count` INT(11) NOT NULL,
    `member_id` BIGINT(20) NULL DEFAULT NULL,
    `parent_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`category_id`),
    INDEX `fkx-category-member` (`member_id` ASC),
    INDEX `fkx-category-category` (`parent_id` ASC),
    CONSTRAINT `fk-category-member`
    FOREIGN KEY (`member_id`)
    REFERENCES `customvoca`.`member` (`member_id`),
    CONSTRAINT `fk-category-category`
    FOREIGN KEY (`parent_id`)
    REFERENCES `customvoca`.`category` (`category_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`hibernate_sequence`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`hibernate_sequence` (
    `next_val` BIGINT(20) NULL DEFAULT NULL)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`member_roles`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`member_roles` (
    `member_member_id` BIGINT(20) NOT NULL,
    `roles` VARCHAR(255) NULL DEFAULT NULL,
    INDEX `fkx-member_roles-member` (`member_member_id` ASC),
    CONSTRAINT `fk-member_roles-member`
    FOREIGN KEY (`member_member_id`)
    REFERENCES `customvoca`.`member` (`member_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`reply`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`reply` (
    `reply_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `children_count` INT(11) NOT NULL,
    `content` VARCHAR(1200) NOT NULL,
    `like_count` INT(11) NOT NULL,
    `register_date` DATETIME(6) NULL DEFAULT NULL,
    `status` INT(11) NOT NULL,
    `bbs_id` BIGINT(20) NOT NULL,
    `member_id` BIGINT(20) NOT NULL,
    `parent_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`reply_id`),
    INDEX `fkx-reply-bbs` (`bbs_id` ASC),
    INDEX `fkx-reply-member` (`member_id` ASC),
    INDEX `fkx-reply-reply` (`parent_id` ASC),
    CONSTRAINT `fk-reply-reply`
    FOREIGN KEY (`parent_id`)
    REFERENCES `customvoca`.`reply` (`reply_id`),
    CONSTRAINT `fk-reply-member`
    FOREIGN KEY (`member_id`)
    REFERENCES `customvoca`.`member` (`member_id`),
    CONSTRAINT `fk-reply-bbs`
    FOREIGN KEY (`bbs_id`)
    REFERENCES `customvoca`.`bbs` (`bbs_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`reply_like`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`reply_like` (
    `reply_like_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `register_date` DATETIME(6) NULL DEFAULT NULL,
    `member_id` BIGINT(20) NULL DEFAULT NULL,
    `reply_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`reply_like_id`),
    INDEX `fkx-reply_like-member` (`member_id` ASC),
    INDEX `fkx-reply_like-reply` (`reply_id` ASC),
    CONSTRAINT `fk-reply_like-member`
    FOREIGN KEY (`member_id`)
    REFERENCES `customvoca`.`member` (`member_id`),
    CONSTRAINT `fk-reply_like-reply`
    FOREIGN KEY (`reply_id`)
    REFERENCES `customvoca`.`reply` (`reply_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`vocabulary`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`vocabulary` (
    `vocabulary_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `difficulty` INT(11) NOT NULL,
    `division` VARCHAR(255) NOT NULL,
    `download_count` INT(11) NOT NULL,
    `like_count` INT(11) NOT NULL,
    `main_language` VARCHAR(255) NOT NULL,
    `memorised_count` INT(11) NOT NULL,
    `register_date` DATETIME(6) NULL,
    `sub_language` VARCHAR(255) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `total_word_count` INT(11) NOT NULL,
    `views` INT(11) NOT NULL,
    `category_id` BIGINT(20) NULL DEFAULT NULL,
    `member_id` BIGINT(20) NULL,
    `writer_id` BIGINT(20) NOT NULL,
    PRIMARY KEY (`vocabulary_id`),
    INDEX `fkx-vocabulary-category` (`category_id` ASC),
    INDEX `fkx-vocabulary-member-1` (`member_id` ASC),
    INDEX `fkx-vocabulary-member-2` (`writer_id` ASC),
    CONSTRAINT `fk-vocabulary-member-2`
    FOREIGN KEY (`writer_id`)
    REFERENCES `customvoca`.`member` (`member_id`),
    CONSTRAINT `fk-vocabulary-member-1`
    FOREIGN KEY (`member_id`)
    REFERENCES `customvoca`.`member` (`member_id`),
    CONSTRAINT `fk-vocabulary-category`
    FOREIGN KEY (`category_id`)
    REFERENCES `customvoca`.`category` (`category_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`vocabulary_like`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`vocabulary_like` (
    `vocabulary_like_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `register_date` DATETIME(6) NOT NULL,
    `member_id` BIGINT(20) NOT NULL,
    `vocabulary_id` BIGINT(20) NOT NULL,
    PRIMARY KEY (`vocabulary_like_id`),
    INDEX `fkx-vocabulary_like-member` (`member_id` ASC),
    INDEX `fkx-vocabulary_like-vocabulary` (`vocabulary_id` ASC),
    CONSTRAINT `fk-vocabulary_like-member`
    FOREIGN KEY (`member_id`)
    REFERENCES `customvoca`.`member` (`member_id`),
    CONSTRAINT `fk-vocabulary_like-vocabulary`
    FOREIGN KEY (`vocabulary_id`)
    REFERENCES `customvoca`.`vocabulary` (`vocabulary_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`vocabulary_thumbnail_image_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`vocabulary_thumbnail_image_file` (
    `vocabulary_thumbnail_image_file_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `file_download_uri` VARCHAR(1000) NULL,
    `file_name` VARCHAR(255) NULL,
    `file_stored_path` VARCHAR(255) NULL,
    `file_type` VARCHAR(255) NULL,
    `size` BIGINT(20) NULL DEFAULT NULL,
    `vocabulary_id` BIGINT(20) NULL,
    PRIMARY KEY (`vocabulary_thumbnail_image_file_id`),
    INDEX `fkx-vocabulary_thumbnail_image_file-vocabulary` (`vocabulary_id` ASC),
    CONSTRAINT `fk-vocabulary_thumbnail_image_file-vocabulary`
    FOREIGN KEY (`vocabulary_id`)
    REFERENCES `customvoca`.`vocabulary` (`vocabulary_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`word`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`word` (
    `word_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `main_word` VARCHAR(255) NULL,
    `memorised_check` BIT(1) NOT NULL,
    `sub_word` VARCHAR(255) NULL DEFAULT NULL,
    `vocabulary_id` BIGINT(20) NULL,
    PRIMARY KEY (`word_id`),
    INDEX `fkx-word-vocabulary` (`vocabulary_id` ASC),
    CONSTRAINT `fk-word-vocabulary`
    FOREIGN KEY (`vocabulary_id`)
    REFERENCES `customvoca`.`vocabulary` (`vocabulary_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `customvoca`.`word_image_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customvoca`.`word_image_file` (
    `word_image_file_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `file_download_uri` VARCHAR(1000) NULL DEFAULT NULL,
    `file_name` VARCHAR(255) NULL DEFAULT NULL,
    `file_stored_path` VARCHAR(255) NULL DEFAULT NULL,
    `file_type` VARCHAR(255) NULL DEFAULT NULL,
    `size` BIGINT(20) NULL DEFAULT NULL,
    `word_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`word_image_file_id`),
    INDEX `fkx-word_image_file-word` (`word_id` ASC),
    CONSTRAINT `fk-word_image_file-word`
    FOREIGN KEY (`word_id`)
    REFERENCES `customvoca`.`word` (`word_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8;
