CREATE TABLE "products" (
    "id" UUID NOT NULL,
    CONSTRAINT "products_pk" PRIMARY KEY ("id")
);
CREATE TABLE "names" (
    "product_id" UUID NOT NULL,
    "lang_code" VARCHAR(2) NOT NULL,
    "name" TEXT NOT NULL,
    CONSTRAINT "names_pk"
    PRIMARY KEY ("product_id", "lang_code"),
    CONSTRAINT "names_product_id_fk"
    FOREIGN KEY ("product_id")
    REFERENCES "products" ("id")
    ON DELETE CASCADE ON UPDATE CASCADE
);