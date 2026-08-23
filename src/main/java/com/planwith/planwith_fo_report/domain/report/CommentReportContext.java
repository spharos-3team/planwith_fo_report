package com.planwith.planwith_fo_report.domain.report;

import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.exception.CommentNotReportableException;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;
import com.planwith.planwith_fo_report.domain.report.exception.SelfCommentReportException;

public class CommentReportContext {

	private final UUID commentUuid;
	private final UUID authorMemberUuid;
	private final boolean reportable;

	private CommentReportContext(UUID commentUuid, UUID authorMemberUuid, boolean reportable) {
		this.commentUuid = commentUuid;
		this.authorMemberUuid = authorMemberUuid;
		this.reportable = reportable;
	}

	public static CommentReportContext of(UUID commentUuid, UUID authorMemberUuid, boolean reportable) {
		if (commentUuid == null) {
			throw new InvalidReportException("댓글 UUID는 필수입니다.");
		}
		if (reportable && authorMemberUuid == null) {
			throw new InvalidReportException("댓글 작성자 UUID는 필수입니다.");
		}
		return new CommentReportContext(commentUuid, authorMemberUuid, reportable);
	}

	public void assertCanBeReportedBy(UUID reporterMemberUuid) {
		if (reporterMemberUuid == null) {
			throw new InvalidReportException("회원 UUID는 필수입니다.");
		}
		if (!reportable) {
			throw new CommentNotReportableException(commentUuid);
		}
		if (authorMemberUuid.equals(reporterMemberUuid)) {
			throw new SelfCommentReportException();
		}
	}

	public UUID getCommentUuid() {
		return commentUuid;
	}

	public UUID getAuthorMemberUuid() {
		return authorMemberUuid;
	}

	public boolean isReportable() {
		return reportable;
	}
}
