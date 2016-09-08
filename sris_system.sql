SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `SRIS_SYSTEM` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `SRIS_SYSTEM` ;

-- -----------------------------------------------------
-- Table `SRIS_SYSTEM`.`Roles`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SRIS_SYSTEM`.`Roles` (
  `RoleId` INT NOT NULL AUTO_INCREMENT ,
  `Name` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`RoleId`) ,
  UNIQUE INDEX `RoleName_UNIQUE` (`Name` ASC) )
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `SRIS_SYSTEM`.`Colleges`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SRIS_SYSTEM`.`Colleges` (
  `CollegeId` INT NOT NULL AUTO_INCREMENT ,
  `Name` VARCHAR(45) NOT NULL ,
  `Type` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`CollegeId`) ,
  UNIQUE INDEX `Name_UNIQUE` (`Name` ASC) ,
  UNIQUE INDEX `Type_UNIQUE` (`Type` ASC) )
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `SRIS_SYSTEM`.`Users`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SRIS_SYSTEM`.`Users` (
  `UserId` INT NOT NULL AUTO_INCREMENT ,
  `Username` VARCHAR(45) NOT NULL ,
  `Password` VARCHAR(45) NOT NULL ,
  `RoleId` INT NOT NULL ,
  `RegDate` TIMESTAMP NULL ,
  `LastLogin` TIMESTAMP NULL ,
  `Ban` TINYINT(1)  NOT NULL DEFAULT 0 ,
  `CollegeId` INT NOT NULL ,
  `Rating` INT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`UserId`, `Username`) ,
  UNIQUE INDEX `Username_UNIQUE` (`Username` ASC) ,
  UNIQUE INDEX `Password_UNIQUE` (`Password` ASC) ,
  UNIQUE INDEX `RoleId_UNIQUE` (`RoleId` ASC) ,
  INDEX `RoleId` (`RoleId` ASC) ,
  INDEX `CollegeId` (`CollegeId` ASC) ,
  CONSTRAINT `RoleId`
    FOREIGN KEY (`RoleId` )
    REFERENCES `SRIS_SYSTEM`.`Roles` (`RoleId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CollegeId`
    FOREIGN KEY (`CollegeId` )
    REFERENCES `SRIS_SYSTEM`.`Colleges` (`CollegeId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `SRIS_SYSTEM`.`Subjects`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SRIS_SYSTEM`.`Subjects` (
  `SubjectId` INT NOT NULL AUTO_INCREMENT ,
  `Name` VARCHAR(45) NOT NULL ,
  `CollegeId` INT NOT NULL ,
  PRIMARY KEY (`SubjectId`) ,
  UNIQUE INDEX `Name_UNIQUE` (`Name` ASC) ,
  INDEX `CollegeId` (`CollegeId` ASC) ,
  CONSTRAINT `CollegeId`
    FOREIGN KEY (`CollegeId` )
    REFERENCES `SRIS_SYSTEM`.`Colleges` (`CollegeId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `SRIS_SYSTEM`.`Threads`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SRIS_SYSTEM`.`Threads` (
  `ThreadId` INT NOT NULL AUTO_INCREMENT ,
  `SubjectId` INT NOT NULL ,
  `UserId` INT NOT NULL ,
  `CreationDate` TIMESTAMP NULL ,
  `BucketId` INT NOT NULL ,
  `TotalDocs` INT NULL DEFAULT 0 ,
  `Name` VARCHAR(45) NOT NULL DEFAULT 'NEW THREAD' ,
  PRIMARY KEY (`ThreadId`) ,
  INDEX `SubjectId` (`SubjectId` ASC) ,
  INDEX `UserId` (`UserId` ASC) ,
  CONSTRAINT `SubjectId`
    FOREIGN KEY (`SubjectId` )
    REFERENCES `SRIS_SYSTEM`.`Subjects` (`SubjectId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `UserId`
    FOREIGN KEY (`UserId` )
    REFERENCES `SRIS_SYSTEM`.`Users` (`UserId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `SRIS_SYSTEM`.`S3_Buckets`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SRIS_SYSTEM`.`S3_Buckets` (
  `ThreadId` INT NOT NULL ,
  `BucketId` INT NOT NULL ,
  `Name` VARCHAR(45) NOT NULL ,
  `Date` TIMESTAMP NULL ,
  `Block` TINYINT(1)  NOT NULL DEFAULT 0 ,
  `Owner` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`BucketId`) ,
  INDEX `ThreadId` (`ThreadId` ASC) ,
  UNIQUE INDEX `Name_UNIQUE` (`Name` ASC) ,
  CONSTRAINT `ThreadId`
    FOREIGN KEY (`ThreadId` )
    REFERENCES `SRIS_SYSTEM`.`Threads` (`ThreadId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `SRIS_SYSTEM`.`DocFormats`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SRIS_SYSTEM`.`DocFormats` (
  `FormatId` INT NOT NULL ,
  `Name` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`FormatId`) )
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `SRIS_SYSTEM`.`Documents`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SRIS_SYSTEM`.`Documents` (
  `DocumentId` INT NOT NULL ,
  `FormatId` INT NOT NULL ,
  `BucketId` INT NOT NULL ,
  `Size` INT NOT NULL ,
  `Date` TIMESTAMP NULL ,
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
    REFERENCES `SRIS_SYSTEM`.`S3_Buckets` (`BucketId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `UserId`
    FOREIGN KEY (`UserId` )
    REFERENCES `SRIS_SYSTEM`.`Users` (`UserId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FormatId`
    FOREIGN KEY (`FormatId` )
    REFERENCES `SRIS_SYSTEM`.`DocFormats` (`FormatId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `SRIS_SYSTEM`.`Comments`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SRIS_SYSTEM`.`Comments` (
  `DocumentId` INT NULL ,
  `Comment` VARCHAR(300) NOT NULL ,
  `Date` TIMESTAMP NULL ,
  `UserId` INT NOT NULL ,
  `Hide` TINYINT(1)  NOT NULL DEFAULT 0 ,
  INDEX `DocumentId` (`DocumentId` ASC) ,
  INDEX `UserId` (`UserId` ASC) ,
  CONSTRAINT `DocumentId`
    FOREIGN KEY (`DocumentId` )
    REFERENCES `SRIS_SYSTEM`.`Documents` (`DocumentId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `UserId`
    FOREIGN KEY (`UserId` )
    REFERENCES `SRIS_SYSTEM`.`Users` (`UserId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `SRIS_SYSTEM`.`Subjects_Taken`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SRIS_SYSTEM`.`Subjects_Taken` (
  `UserId` INT NOT NULL ,
  `SubjectId` INT NOT NULL ,
  INDEX `UserId` (`UserId` ASC) ,
  INDEX `SubjectId` (`SubjectId` ASC) ,
  CONSTRAINT `UserId`
    FOREIGN KEY (`UserId` )
    REFERENCES `SRIS_SYSTEM`.`Users` (`UserId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `SubjectId`
    FOREIGN KEY (`SubjectId` )
    REFERENCES `SRIS_SYSTEM`.`Subjects` (`SubjectId` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `SRIS_SYSTEM`.`Index_Table`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `SRIS_SYSTEM`.`Index_Table` (
  `Id` INT NOT NULL ,
  `Title` VARCHAR(45) NULL ,
  `Rating` INT NOT NULL DEFAULT 0 ,
  `Description` VARCHAR(150) NULL ,
  `Date` TIMESTAMP NULL ,
  `Url` VARCHAR(400) NOT NULL ,
  `UserId` INT NOT NULL ,
  `rawtext` VARCHAR(45) NULL ,
  `Subject` VARCHAR(45) NULL ,
  `CollegeId` INT NULL ,
  PRIMARY KEY (`Id`) )
ENGINE = MyISAM;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
