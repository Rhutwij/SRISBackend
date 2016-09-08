SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `sris` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `sris` ;

-- -----------------------------------------------------
-- Table `sris`.`Roles`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sris`.`Roles` (
  `RoleId` INT NOT NULL AUTO_INCREMENT ,
  `Name` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`RoleId`) ,
  UNIQUE INDEX `RoleName_UNIQUE` (`Name` ASC) )
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `sris`.`Colleges`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sris`.`Colleges` (
  `CollegeId` INT NOT NULL AUTO_INCREMENT ,
  `Name` VARCHAR(45) NOT NULL ,
  `Type` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`CollegeId`) ,
  UNIQUE INDEX `Name_UNIQUE` (`Name` ASC) ,
  UNIQUE INDEX `Type_UNIQUE` (`Type` ASC) )
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `sris`.`Users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sris`.`Users` (
  `UserId` INT NOT NULL AUTO_INCREMENT ,
  `Username` VARCHAR(45) NOT NULL ,
  `Password` VARCHAR(45) NOT NULL ,
  `RoleId` INT NOT NULL ,
  `RegDate` DATETIME,
  `LastLogin` DATETIME,
  `Ban` TINYINT(1)  NOT NULL DEFAULT 0 ,
  `CollegeId` INT NOT NULL ,
  `Rating` INT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`UserId`) ,
  UNIQUE INDEX `Username_UNIQUE` (`Username` ASC) ,
  UNIQUE INDEX `Password_UNIQUE` (`Password` ASC) ,
  UNIQUE INDEX `RoleId_UNIQUE` (`RoleId` ASC) ,
  INDEX `RoleId` (`RoleId` ASC) ,
  INDEX `CollegeId` (`CollegeId` ASC) ,
  CONSTRAINT `RoleId`
    FOREIGN KEY (`RoleId` )
    REFERENCES `sris`.`Roles` (`RoleId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CollegeId`
    FOREIGN KEY (`CollegeId` )
    REFERENCES `sris`.`Colleges` (`CollegeId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `sris`.`Subjects`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sris`.`Subjects` (
  `SubjectId` INT NOT NULL AUTO_INCREMENT ,
  `Name` VARCHAR(45) NOT NULL ,
  `CollegeId` INT NOT NULL ,
  PRIMARY KEY (`SubjectId`) ,
  UNIQUE INDEX `Name_UNIQUE` (`Name` ASC) ,
  INDEX `CollegeId` (`CollegeId` ASC) ,
  CONSTRAINT `CollegeId`
    FOREIGN KEY (`CollegeId` )
    REFERENCES `sris`.`Colleges` (`CollegeId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `sris`.`Threads`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sris`.`Threads` (
  `ThreadId` INT NOT NULL AUTO_INCREMENT ,
  `SubjectId` INT NOT NULL ,
  `UserId` INT NOT NULL ,
  `CreationDate` DATETIME,
  `BucketId` INT NOT NULL ,
  `TotalDocs` INT NOT NULL DEFAULT 0 ,
  `Name` VARCHAR(45) NOT NULL DEFAULT 'NEW THREAD' ,
  PRIMARY KEY (`ThreadId`) ,
  INDEX `SubjectId` (`SubjectId` ASC) ,
  INDEX `UserId` (`UserId` ASC) ,
  CONSTRAINT `SubjectId`
    FOREIGN KEY (`SubjectId` )
    REFERENCES `sris`.`Subjects` (`SubjectId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `UserId`
    FOREIGN KEY (`UserId` )
    REFERENCES `sris`.`Users` (`UserId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `sris`.`S3Buckets`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sris`.`S3Buckets` (
  `ThreadId` INT NOT NULL ,
  `BucketId` INT NOT NULL ,
  `Name` VARCHAR(45) NOT NULL ,
  `Date` DATETIME,
  `Block` TINYINT(1)  NOT NULL DEFAULT 0 ,
  `Owner` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`BucketId`) ,
  INDEX `ThreadId` (`ThreadId` ASC) ,
  CONSTRAINT `ThreadId`
    FOREIGN KEY (`ThreadId` )
    REFERENCES `sris`.`Threads` (`ThreadId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `sris`.`DocFormats`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sris`.`DocFormats` (
  `FormatId` INT NOT NULL ,
  `Name` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`FormatId`) )
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `sris`.`Documents`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sris`.`Documents` (
  `DocumentId` INT NOT NULL ,
  `FormatId` INT NOT NULL ,
  `BucketId` INT NOT NULL ,
  `Size` INT NOT NULL ,
  `Date` DATETIME,
  `UserId` INT NOT NULL ,
  `Title` VARCHAR(45) NOT NULL ,
  `Rating` INT NOT NULL DEFAULT 0 ,
  `Hide` TINYINT(1)  NOT NULL DEFAULT 0 ,
  `Url` VARCHAR(400) NOT NULL ,
  `Desc` VARCHAR(45) NULL ,
  `key` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`DocumentId`) ,
  INDEX `BucketId` (`BucketId` ASC) ,
  INDEX `UserId` (`UserId` ASC) ,
  INDEX `FormatId` (`FormatId` ASC) ,
  UNIQUE INDEX `key_UNIQUE` (`key` ASC) ,
  CONSTRAINT `BucketId`
    FOREIGN KEY (`BucketId` )
    REFERENCES `sris`.`S3Buckets` (`BucketId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `UserId`
    FOREIGN KEY (`UserId` )
    REFERENCES `sris`.`Users` (`UserId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FormatId`
    FOREIGN KEY (`FormatId` )
    REFERENCES `sris`.`DocFormats` (`FormatId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `sris`.`Comments`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sris`.`Comments` (
  `DocumentId` INT NOT NULL ,
  `Comment` VARCHAR(300) NOT NULL ,
  `Date` DATETIME,
  `UserId` INT NOT NULL ,
  `Hide` TINYINT(1)  NOT NULL DEFAULT 0 ,
  INDEX `DocumentId` (`DocumentId` ASC) ,
  INDEX `UserId` (`UserId` ASC) ,
  CONSTRAINT `DocumentId`
    FOREIGN KEY (`DocumentId` )
    REFERENCES `sris`.`Documents` (`DocumentId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `UserId`
    FOREIGN KEY (`UserId` )
    REFERENCES `sris`.`Users` (`UserId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `sris`.`Subjects Taken`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sris`.`Subjects Taken` (
  `UserId` INT NOT NULL ,
  `SubjectId` INT NOT NULL ,
  INDEX `UserId` (`UserId` ASC) ,
  INDEX `SubjectId` (`SubjectId` ASC) ,
  CONSTRAINT `UserId`
    FOREIGN KEY (`UserId` )
    REFERENCES `sris`.`Users` (`UserId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `SubjectId`
    FOREIGN KEY (`SubjectId` )
    REFERENCES `sris`.`Subjects` (`SubjectId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `sris`.`Index Table`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `sris`.`Index Table` (
  `Id` INT NOT NULL ,
  `Title` VARCHAR(45) NULL ,
  `Rating` INT NOT NULL DEFAULT 0 ,
  `Description` VARCHAR(150) NOT NULL ,
  `Date` DATETIME,
  `Url` VARCHAR(400) NOT NULL ,
  `OwnerId` INT NOT NULL ,
  PRIMARY KEY (`Id`) )
ENGINE = MyISAM;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
