package com.planwith.planwith_fo_report.adapter.out.comment;

import java.util.UUID;

record CommentReportContextResponse(
		UUID commentUuid,
		UUID authorMemberUuid,
		Boolean reportable
) {
}
