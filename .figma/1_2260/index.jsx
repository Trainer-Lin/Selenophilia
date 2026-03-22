import React from 'react';

import styles from './index.module.scss';

const Component = () => {
  return (
    <div className={styles.banner}>
      <p className={styles.bannerTitle}>Banner title</p>
      <div className={styles.pagination}>
        <div className={styles.selected} />
        <div className={styles.default} />
        <div className={styles.default} />
        <div className={styles.default} />
        <div className={styles.default} />
      </div>
    </div>
  );
}

export default Component;
