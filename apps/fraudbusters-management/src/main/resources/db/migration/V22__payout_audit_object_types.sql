-- Payout changes are audited apart from payment ones.
ALTER TYPE af.object_type ADD VALUE 'payout_template';
ALTER TYPE af.object_type ADD VALUE 'payout_reference';
ALTER TYPE af.object_type ADD VALUE 'payout_group_reference';
ALTER TYPE af.object_type ADD VALUE 'payout_group';
ALTER TYPE af.object_type ADD VALUE 'payout_black';
ALTER TYPE af.object_type ADD VALUE 'payout_white';
ALTER TYPE af.object_type ADD VALUE 'payout_grey';
ALTER TYPE af.object_type ADD VALUE 'payout_naming';
