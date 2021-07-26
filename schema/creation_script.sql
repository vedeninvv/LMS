CREATE TABLE `Users`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `surname` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `avatar` BLOB NULL,
    `registration_date` DATE NOT NULL,
    `last_update_date` DATE NULL,
    `last_update_author` INT NULL,
    `delete_date` DATE NULL,
    `delete_author` INT NULL,
    `admin` TINYINT(1) NOT NULL
);
ALTER TABLE
    `Users` ADD PRIMARY KEY `users_id_primary`(`id`);
CREATE TABLE `Courses`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NOT NULL,
    `creation_date` DATE NOT NULL,
    `creation_author` INT NOT NULL,
    `course_time` INT NOT NULL,
    `change_date` DATE NULL,
    `change_author` INT NULL,
    `delete_date` DATE NULL,
    `delete_author` INT NULL,
    `rating` INT NOT NULL,
    `tag` VARCHAR(255) NOT NULL,
    `category` VARCHAR(255) NOT NULL
);
ALTER TABLE
    `Courses` ADD PRIMARY KEY `courses_id_primary`(`id`);
CREATE TABLE `Modules`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NOT NULL,
    `creation_date` DATE NOT NULL,
    `creation_author` INT NOT NULL,
    `change_date` DATE NULL,
    `change_author` INT NULL,
    `delete_date` DATE NULL,
    `delete_author` INT NULL,
    `course_id` INT NOT NULL
);
ALTER TABLE
    `Modules` ADD PRIMARY KEY `modules_id_primary`(`id`);
CREATE TABLE `Themes`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NOT NULL,
    `creation_date` DATE NOT NULL,
    `creation_author` INT NOT NULL,
    `change_date` DATE NULL,
    `change_author` INT NULL,
    `delete_date` DATE NULL,
    `delete_author` INT NULL,
    `module_id` INT NOT NULL
);
ALTER TABLE
    `Themes` ADD PRIMARY KEY `themes_id_primary`(`id`);
CREATE TABLE `Tasks`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `theme_id` INT NOT NULL,
    `task_text` TEXT NOT NULL
);
ALTER TABLE
    `Tasks` ADD PRIMARY KEY `tasks_id_primary`(`id`);
CREATE TABLE `Content`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `theme_id` INT NOT NULL,
    `content` BLOB NOT NULL
);
ALTER TABLE
    `Content` ADD PRIMARY KEY `content_id_primary`(`id`);
CREATE TABLE `Rating`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `course_id` INT NOT NULL
);
ALTER TABLE
    `Rating` ADD PRIMARY KEY `rating_id_primary`(`id`);
CREATE TABLE `users_courses`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `course_id` INT NOT NULL
);
ALTER TABLE
    `users_courses` ADD PRIMARY KEY `users_courses_id_primary`(`id`);
ALTER TABLE
    `Rating` ADD CONSTRAINT `rating_course_id_foreign` FOREIGN KEY(`course_id`) REFERENCES `Courses`(`id`);
ALTER TABLE
    `users_courses` ADD CONSTRAINT `users_courses_course_id_foreign` FOREIGN KEY(`course_id`) REFERENCES `Courses`(`id`);
ALTER TABLE
    `Courses` ADD CONSTRAINT `courses_creation_author_foreign` FOREIGN KEY(`creation_author`) REFERENCES `Users`(`id`);
ALTER TABLE
    `Courses` ADD CONSTRAINT `courses_change_author_foreign` FOREIGN KEY(`change_author`) REFERENCES `Users`(`id`);
ALTER TABLE
    `Courses` ADD CONSTRAINT `courses_delete_author_foreign` FOREIGN KEY(`delete_author`) REFERENCES `Users`(`id`);
ALTER TABLE
    `Modules` ADD CONSTRAINT `modules_creation_author_foreign` FOREIGN KEY(`creation_author`) REFERENCES `Users`(`id`);
ALTER TABLE
    `Modules` ADD CONSTRAINT `modules_change_author_foreign` FOREIGN KEY(`change_author`) REFERENCES `Users`(`id`);
ALTER TABLE
    `Modules` ADD CONSTRAINT `modules_delete_author_foreign` FOREIGN KEY(`delete_author`) REFERENCES `Users`(`id`);
ALTER TABLE
    `Modules` ADD CONSTRAINT `modules_course_id_foreign` FOREIGN KEY(`course_id`) REFERENCES `Courses`(`id`);
ALTER TABLE
    `Themes` ADD CONSTRAINT `themes_creation_author_foreign` FOREIGN KEY(`creation_author`) REFERENCES `Users`(`id`);
ALTER TABLE
    `Themes` ADD CONSTRAINT `themes_change_author_foreign` FOREIGN KEY(`change_author`) REFERENCES `Users`(`id`);
ALTER TABLE
    `Themes` ADD CONSTRAINT `themes_delete_author_foreign` FOREIGN KEY(`delete_author`) REFERENCES `Users`(`id`);
ALTER TABLE
    `Themes` ADD CONSTRAINT `themes_module_id_foreign` FOREIGN KEY(`module_id`) REFERENCES `Modules`(`id`);
ALTER TABLE
    `Content` ADD CONSTRAINT `content_theme_id_foreign` FOREIGN KEY(`theme_id`) REFERENCES `Themes`(`id`);
ALTER TABLE
    `Tasks` ADD CONSTRAINT `tasks_theme_id_foreign` FOREIGN KEY(`theme_id`) REFERENCES `Themes`(`id`);
ALTER TABLE
    `users_courses` ADD CONSTRAINT `users_courses_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `Users`(`id`);
ALTER TABLE
    `Rating` ADD CONSTRAINT `rating_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `Users`(`id`);