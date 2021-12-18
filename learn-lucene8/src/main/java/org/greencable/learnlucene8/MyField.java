package org.greencable.learnlucene8;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableFieldType;

import java.text.Format;
import java.util.Map;

public class MyField extends Field {

    private static final FieldType TYPE = new FieldType();

    static {
        TYPE.setStoreTermVectors(true);
        TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    }

    protected MyField(String name) {
        super(name, TYPE);
    }
}
