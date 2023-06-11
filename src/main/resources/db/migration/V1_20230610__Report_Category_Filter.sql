CREATE TYPE report_category_filter_type AS ENUM (
    'INCLUDE_NO_UNKNOWN',
    'INCLUDE_WITH_UNKNOWN',
    'EXCLUDE_NO_UNKNOWN',
    'EXCLUDE_WITH_UNKNOWN',
    'ALL_NO_UNKNOWN',
    'ALL_WITH_UNKNOWN',
    'NONE_WITH_UNKNOWN'
);

CREATE FUNCTION is_report_category_allowed (
    category_id UUID,
    filter_type report_category_filter_type,
    category_ids UUID[]
)
RETURNS BOOLEAN
AS
$$
    SELECT CASE
        WHEN filter_type = 'INCLUDE_NO_UNKNOWN'::report_category_filter_type THEN (
            category_id = ANY(category_ids)
        )
        WHEN filter_type = 'INCLUDE_WITH_UNKNOWN'::report_category_filter_type THEN (
            category_id = ANY(category_ids) OR category_id IS NULL
        )
        WHEN filter_type = 'EXCLUDE_NO_UNKNOWN'::report_category_filter_type THEN (
            NOT(category_id = ANY(category_ids)) AND category_id IS NOT NULL
        )
        WHEN filter_type = 'EXCLUDE_WITH_UNKNOWN'::report_category_filter_type THEN (
            NOT(category_id = ANY(category_ids)) OR category_id IS NULL
        )
        WHEN filter_type = 'ALL_NO_UNKNOWN'::report_category_filter_type THEN (
            category_id IS NOT NULL
        )
        WHEN filter_type = 'NONE_WITH_UNKNOWN'::report_category_filter_type THEN (
            category_id IS NULL
        )
        ELSE true = true
    END;
$$ LANGUAGE sql;